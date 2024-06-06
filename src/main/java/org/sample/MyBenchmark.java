package org.sample;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

public class MyBenchmark {
    private final static int N = 100_000_000;

    @State(Scope.Benchmark)
    public static class InsnState {
        Insn[] insns;

        @Setup
        public void setup() {
            insns = new Insn[N];
            Random random = new Random(1234);
            for (int i = 0; i < N; i++) {
                Insn ins = switch(randomKind(random)) {
                    case Foo -> new FooInsn();
                    case Bar -> new BarInsn();
                    case Baz -> new BazInsn();
                    case FooBar -> new FooBarInsn();
                };
                insns[i] = ins;
            }
        }

        @TearDown
        public void tearDown() {
            insns = null;
        }
    }


    private static Kind randomKind(Random rng) {
        Kind[] values = Kind.values();
        return values[rng.nextInt(values.length)];
    }

    @State(Scope.Benchmark)
    public static class InsnImpState {
        InsnImp[] insns;

        @Setup
        public void setup() {
            insns = new InsnImp[N];
            Random random = new Random(1234);
            for (int i = 0; i < N; i++) {
                InsnImp ins = switch(randomKind(random)) {
                    case Foo -> new FooImp();
                    case Bar -> new BarImp();
                    case Baz -> new BazImp();
                    case FooBar -> new FooBarImp();
                };
                insns[i] = ins;
            }
        }
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    //@Fork(value = 1, jvmArgsPrepend = {
    //        "-XX:+UnlockDiagnosticVMOptions",
    //        "-XX:+LogCompilation",
    //        "-XX:+PrintInlining",
    //        "-XX:LogFile=tagAndCast.log"
    //})
    public void tagAndCast(Blackhole blackhole, InsnState state) {
        for (var insn : state.insns) {
            switch (insn.kind()) {
                case Foo -> {
                    FooInsn fooInsn = (FooInsn) insn;
                    blackhole.consume(fooInsn.foo());
                }
                case Bar -> {
                    BarInsn barInsn = (BarInsn) insn;
                    blackhole.consume(barInsn.bar());
                }
                case Baz -> {
                    BazInsn bazInsn = (BazInsn) insn;
                    blackhole.consume(bazInsn.baz());
                }
                case FooBar -> {
                    FooBarInsn fooBarInsn = (FooBarInsn) insn;
                    blackhole.consume(fooBarInsn.fooBar());
                }
            }
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    //@Fork(value = 1, jvmArgsPrepend = {
    //        "-XX:+UnlockDiagnosticVMOptions",
    //        "-XX:+LogCompilation",
    //        "-XX:+PrintInlining",
    //        "-XX:LogFile=tagAndCastImp.log"
    //})
    public void tagAndCastImp(Blackhole blackhole, InsnImpState state) {
        for (var insn : state.insns) {
            switch (insn.kind()) {
                case Foo -> {
                    FooImp fooInsn = (FooImp) insn;
                    blackhole.consume(fooInsn.foo());
                }
                case Bar -> {
                    BarImp barInsn = (BarImp) insn;
                    blackhole.consume(barInsn.bar());
                }
                case Baz -> {
                    BazImp bazInsn = (BazImp) insn;
                    blackhole.consume(bazInsn.baz());
                }
                case FooBar -> {
                    FooBarImp fooBarInsn = (FooBarImp) insn;
                    blackhole.consume(fooBarInsn.fooBar());
                }
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    //@Fork(value = 1, jvmArgsPrepend = {
    //        "-XX:+UnlockDiagnosticVMOptions",
    //        "-XX:+LogCompilation",
    //        "-XX:+PrintInlining",
    //        "-XX:LogFile=instanceOfAndCall.log"
    //})
    public void instanceOfAndCall(Blackhole blackhole, InsnState state) {
        for (var insn : state.insns) {
            if (insn instanceof FooInsn fooInsn) {
                blackhole.consume(fooInsn.foo());
            } else if (insn instanceof BarInsn barInsn) {
                blackhole.consume(barInsn.bar());
            } else if (insn instanceof BazInsn bazInsn) {
                blackhole.consume(bazInsn.baz());
            } else if (insn instanceof FooBarInsn fooBarInsn) {
                blackhole.consume(fooBarInsn.fooBar());
            }
        }
    }
}

enum Kind {
    Foo,
    Bar,
    Baz,
    FooBar,
}

interface Insn {
    Kind kind();
}

class FooInsn implements Insn {
    @Override
    public Kind kind() {
        return Kind.Foo;
    }

    int foo() {
        return 0;
    }
}

class BarInsn implements Insn {
    @Override
    public Kind kind() {
        return Kind.Bar;
    }

    String bar() {
        return "";
    }
}

class BazInsn implements Insn {
    @Override
    public Kind kind() {
        return Kind.Baz;
    }

    double baz() {
        return 0.0;
    }
}

class FooBarInsn implements Insn {
    @Override
    public Kind kind() {
        return Kind.FooBar;
    }

    long fooBar() {
        return 0L;
    }
}

// This is so that CHA can figure out only one implementation of kind exists. Otherwise creating other Insn (probably)
// disables devirtualization.
interface InsnImp {
    Kind kind();
}

abstract class InsnBase implements InsnImp {
    private final Kind kind;

    InsnBase(Kind kind) {
        this.kind = kind;
    }

    @Override
    public final Kind kind() {
        return kind;
    }
}

class FooImp extends InsnBase {
    FooImp() {
        super(Kind.Foo);
    }

    int foo() {
        return 0;
    }
}

class BarImp extends InsnBase {
    BarImp() {
        super(Kind.Bar);
    }

    String bar() {
        return "";
    }
}

class BazImp extends InsnBase {
    BazImp() {
        super(Kind.Baz);
    }

    double baz() {
        return 0.0;
    }
}

class FooBarImp extends InsnBase {
    FooBarImp() {
        super(Kind.FooBar);
    }

    long fooBar() {
        return 0L;
    }
}
