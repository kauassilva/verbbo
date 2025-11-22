package br.com.codegen;

import java.util.ArrayList;
import java.util.List;

public class CodeWriter {
    private final StringBuilder sb = new StringBuilder();
    private int indentLevel = 0;
    private final String indentUnit = "    "; // 4 spaces

    public void indent() {
        indentLevel++;
    }

    public void unindent() {
        if (indentLevel > 0) indentLevel--;
    }

    public void write(String s) {
        sb.append(s);
    }

    public void writeLine(String line) {
        for (int i = 0; i < indentLevel; i++) sb.append(indentUnit);
        sb.append(line).append('\n');
    }

    public void writeEmptyLine() {
        sb.append('\n');
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}

