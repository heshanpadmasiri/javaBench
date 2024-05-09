/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

public class MyBenchmark {
    private final static int N = 100_000_000;
    private final static Insn[] insns;

    static {
        insns = new Insn[N];
        for (int i = 0; i < N; i++) {
            insns[i] = InstructionBuilder.getRandomInsn();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 0)
    public void tagAndCast(Blackhole blackhole) {
        for(var insn: insns) {
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
    @Fork(value = 1, warmups = 0)
    public void instanceOfAndCall(Blackhole blackhole) {
        for(var insn: insns) {
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

final class InstructionBuilder {
    static Insn getRandomInsn() {
        return switch (Kind.values()[new Random().nextInt(Kind.values().length)]) {
            case Foo -> new FooInsn();
            case Bar -> new BarInsn();
            case Baz -> new BazInsn();
            case FooBar -> new FooBarInsn();
            default -> throw new IllegalStateException();
        };
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