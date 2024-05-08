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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;

public class MyBenchmark {
    private final static int N = 10_000_000;
    private static final TableInstruction[] tableInstructions;
    static {
        tableInstructions = new TableInstruction[N];
        for (int i = 0; i < N; i++) {
            tableInstructions[i] = TableInstruction.getRandom();
        }
    }

    private static final DataInstruction[] dataInstruction;
    static {
        dataInstruction = new DataInstruction[N];
        for (int i = 0; i < N; i++) {
            dataInstruction[i] = DataInstruction.getRandom();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 0)
    public void opTable(Blackhole blackhole) {
        for (TableInstruction each: tableInstructions) {
            TabledOp op = OpsTable.OPS[each.code()];
            TableOpData res = op.addition(each.v1(), each.v2());
            blackhole.consume(res);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 0)
    public void virtFn(Blackhole blackhole) {
        for(var each: dataInstruction) {
            Data res = each.v1().addition(each.v2());
            blackhole.consume(res);
        }
    }
}

record DataInstruction(Data v1, Data v2) {
    static DataInstruction getRandom() {
        Random rand = new Random();
        Data v1, v2;
        switch (rand.nextInt(4)) {
            case 0: {
                v1 = new IntData(rand.nextInt());
                v2 = new IntData(rand.nextInt());
                break;
            }
            case 1: {
                v1 = new DoubleData(rand.nextDouble());
                v2 = new DoubleData(rand.nextDouble());
                break;
            }
            case 2: {
                v1 = new StringData("Hello");
                v2 = new StringData("World");
                break;
            }
            case 3: {
                v1 = new FloatData(rand.nextFloat());
                v2 = new FloatData(rand.nextFloat());
                break;
            }
            default: {
                throw new RuntimeException("unexpected");
            }
        }
        return new DataInstruction(v1, v2);
    }
}

interface Data {
    Data addition(Data other);
}

final class IntData implements Data {

    int value;

    public IntData(int value) {
        this.value = value;
    }

    @Override
    public Data addition(Data other) {
        IntData otherData = (IntData) other;
        return new IntData(otherData.value + this.value);
    }
}
final class FloatData implements Data {

    float value;

    public FloatData(float value) {
        this.value = value;
    }

    @Override
    public Data addition(Data other) {
        FloatData otherData = (FloatData) other;
        return new FloatData(otherData.value + this.value);
    }
}
final class StringData implements Data {

    String value;

    public StringData(String value) {
        this.value = value;
    }

    @Override
    public Data addition(Data other) {
        StringData otherData = (StringData) other;
        return new StringData(otherData.value + this.value);
    }
}
final class DoubleData implements Data {

    double value;

    public DoubleData(double value) {
        this.value = value;
    }

    @Override
    public Data addition(Data other) {
        DoubleData otherData = (DoubleData) other;
        return new DoubleData(otherData.value + this.value);
    }
}
record TableInstruction(int code, TableOpData v1, TableOpData v2) {
    static TableInstruction getRandom() {
        Random rand = new Random();
        int op = rand.nextInt(4);
        TableOpData v1 = switch (op) {
            case 0 -> new IntegerTableData(rand.nextInt());
            case 1 -> new FloatTableData(rand.nextFloat());
            case 2 -> new StringTableData("Hello");
            case 3 -> new DecimalTableData(rand.nextDouble());
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
        TableOpData v2 = switch (op) {
            case 0 -> new IntegerTableData(rand.nextInt());
            case 1 -> new FloatTableData(rand.nextFloat());
            case 2 -> new StringTableData("World");
            case 3 -> new DecimalTableData(rand.nextDouble());
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
        return new TableInstruction(op, v1, v2);
    }
}

final class OpsTable {
    static final TabledOp[] OPS;

    static {
        OPS = new TabledOp[4];
        OPS[0] = new IntegerTableOp();
        OPS[1] = new FloatTableOp();
        OPS[2] = new StringTableOp();
        OPS[3] = new DecimalTableOp();
    }
}

interface TableOpData {}

interface TabledOp {
    TableOpData addition(TableOpData v1, TableOpData v2);
}

final class IntegerTableData implements TableOpData {
    int value;
    public IntegerTableData(int value) {
        this.value = value;
    }
}

final class FloatTableData implements TableOpData {
    float value;
    public FloatTableData(float value) {
        this.value = value;
    }
}

final class StringTableData implements TableOpData {
    String value;
    public StringTableData(String value) {
        this.value = value;
    }
}

final class DecimalTableData implements TableOpData {
    double value;
    public DecimalTableData(double value) {
        this.value = value;
    }
}

final class IntegerTableOp implements TabledOp {
    @Override
    public TableOpData addition(TableOpData v1, TableOpData v2) {
        return new IntegerTableData(((IntegerTableData) v1).value + ((IntegerTableData) v2).value);
    }
}

final class FloatTableOp implements TabledOp {
    @Override
    public TableOpData addition(TableOpData v1, TableOpData v2) {
        return new FloatTableData(((FloatTableData) v1).value + ((FloatTableData) v2).value);
    }
}

final class StringTableOp implements TabledOp {
    @Override
    public TableOpData addition(TableOpData v1, TableOpData v2) {
        return new StringTableData(((StringTableData) v1).value + ((StringTableData) v2).value);
    }
}

final class DecimalTableOp implements TabledOp {
    @Override
    public TableOpData addition(TableOpData v1, TableOpData v2) {
        return new DecimalTableData(((DecimalTableData) v1).value + ((DecimalTableData) v2).value);
    }
}
