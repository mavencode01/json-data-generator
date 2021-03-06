package com.github.vincentrussell.json.datagenerator.impl;

import com.github.vincentrussell.json.datagenerator.TokenResolver;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Stack;

public class FunctionReplacingReader extends Reader {

    protected PushbackReader pushbackReader = null;
    protected TokenResolver tokenResolver = null;
    protected StringBuilder tokenNameBuffer = new StringBuilder();
    protected String tokenValue = null;
    protected int tokenValueIndex = 0;
    protected Stack<IndexHolder> integerStackForIndexFunction = new Stack<IndexHolder>();

    public FunctionReplacingReader(Reader source, TokenResolver resolver) {
        pushbackReader = new PushbackReader(source, 80);
        tokenResolver = resolver;
        integerStackForIndexFunction.push(new IndexHolder());
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public int read() throws IOException {
        if(this.tokenValue != null){
            if(this.tokenValueIndex < this.tokenValue.length()){
                return this.tokenValue.charAt(this.tokenValueIndex++);
            }
            if(this.tokenValueIndex == this.tokenValue.length()){
                this.tokenValue = null;
                this.tokenValueIndex = 0;
            }
        }

        int data = this.pushbackReader.read();

        if(data == ']') {
            integerStackForIndexFunction.pop();
        }
        if (data == '[') {
            integerStackForIndexFunction.push(new IndexHolder());
        }

        if(data != '{') return data;

        data = this.pushbackReader.read();
        if(data != '{'){
            this.pushbackReader.unread(data);
            return '{';
        }

        this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());

        data = this.pushbackReader.read();
        while(data != '}'){
            this.tokenNameBuffer.append((char) data);
            data = this.pushbackReader.read();
        }

        data = this.pushbackReader.read();

        try {
            this.tokenValue = this.tokenResolver
                .resolveToken(integerStackForIndexFunction.peek(),"{{"+this.tokenNameBuffer.toString()+"}}");
        } catch (IllegalArgumentException e) {
            this.tokenValue = null;
        }

        if(this.tokenValue == null){
            this.tokenValue = "{{"+ this.tokenNameBuffer.toString() + "}}";
        }

        if(this.tokenValue.length() == 0){
            return read();
        }
        return this.tokenValue.charAt(this.tokenValueIndex++);


    }

    @Override
    public int read(char cbuf[]) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public void close() throws IOException {
        this.pushbackReader.close();
    }

    @Override
    public long skip(long n) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public boolean ready() throws IOException {
        return this.pushbackReader.ready();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public void reset() throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

}