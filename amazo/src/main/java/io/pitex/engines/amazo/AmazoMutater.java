package io.pitex.engines.amazo;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.*;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.reloc.asm.ClassReader;
import org.pitest.reloc.asm.ClassWriter;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.MethodNode;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Predicate;

public class AmazoMutater implements Mutater {

    private final ClassByteArraySource source;
    private final HashMap<String, MutationOperator> operators;
    private final Predicate<MethodNode> excludedMethods;

    public AmazoMutater(ClassByteArraySource source, Collection<MutationOperator> operators) {
        this(source, operators, (node) -> false);
    }

    public AmazoMutater(ClassByteArraySource source,
                        Collection<MutationOperator> operators,
                        Predicate<MethodNode> excludedMethods) {
        Objects.requireNonNull(source, "Source code to inspect for mutations can not be null");
        Objects.requireNonNull(operators, "Collection of mutation operators can not be null");
        if(operators.isEmpty())
            throw new IllegalArgumentException("There must be at least one mutaton operator");
        Objects.requireNonNull(excludedMethods,
                "Excluded methods predicate can not be null. Use alternate construction instead");
        this.source = source;
        this.operators = new HashMap<>(operators.size());
        for(MutationOperator operator : Set.copyOf(operators)) {
            this.operators.put(operator.identifier(), operator);
        }
        this.excludedMethods = excludedMethods;
    }

    private ClassNode toNode(byte[] code) {
        ClassNode classNode = new ClassNode();
        new ClassReader(code).accept(classNode, 0);
        return classNode;
    }

    private byte[] toCode(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    @Override
    public List<MutationDetails> findMutations(ClassName className) {
        Optional<byte[]> code = source.getBytes(className.asInternalName());
        if(code.isEmpty()) {
            return Collections.emptyList();
        }
        ClassNode classNode = toNode(code.get());
        List<MutationDetails> result = new ArrayList<>();
        for(MutationOperator operator : operators.values()) {
            for(MethodNode method : classNode.methods) {
                if(!excludedMethods.test(method)) {
                    result.addAll(operator.findMutations(method, classNode));
                }
            }
        }
        return result;
    }

    @Override
    public Mutant getMutation(MutationIdentifier mutationIdentifier) {
        ClassName className = mutationIdentifier.getClassName();
        Optional<byte[]> code = source.getBytes(className.asInternalName());
        if(code.isEmpty()) {
            throw new IllegalStateException("Requesting mutation in unknown class: " + className.asJavaName());
        }
        MutationOperator operator = operators.get(mutationIdentifier.getMutator());
        if(operator == null) {
            throw new IllegalStateException("Requesting mutation with unknown operator " + mutationIdentifier.getMutator());
        }
        ClassNode classNode = toNode(code.get());
        Location location = mutationIdentifier.getLocation();
        Predicate<MethodNode> byLocation = (node) ->
            location.getMethodName().name().equals(node.name) &&
                    location.getMethodDesc().equals(node.desc)
        ;
        Optional<MethodNode> methodNode = classNode.methods.stream().filter(byLocation).findFirst();
        if(methodNode.isEmpty()) {
            throw new IllegalStateException("Requesting mutation in unknown method " + location.getMethodName() + location.getMethodDesc());
        }
        operator.mutate(mutationIdentifier, methodNode.get(), classNode);

        return new Mutant(
                new MutationDetails(mutationIdentifier, classNode.sourceFile, operator.description(),
                        0, 0),
                toCode(classNode)
        );
    }
}
