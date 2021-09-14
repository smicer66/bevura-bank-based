package com.probase.probasepay.util;

import java.io.Serializable;

public class Token implements Comparable<Token>, Serializable {

    private StringBuilder token;

    private int[] parse(StringBuilder token) {
        int[] result = new int[token.length()];

        for (int i = 0; i < token.length(); i++) {
            result[i] = Character.getNumericValue(token.charAt(i));
        }

        return result;
    }

    /**
     *
     * @param token
     */
    public Token(String token) {
        this.token = new StringBuilder(token);
    }

    /**
     *
     * @param capacity
     */
    public Token(int capacity) {
        this.token = new StringBuilder(capacity);
        this.token.setLength(capacity);
    }

    /**
     *
     * @return
     */
    public StringBuilder getToken() {
        return token;
    }

    /**
     *
     * @param i
     * @param digit
     */
    public void setDigitAt(int i, int digit) {
        token.setCharAt(i, Character.forDigit(digit, 10));
    }

    /**
     *
     * @param i
     * @return
     */
    public int getDigitAt(int i) {
        return Character.getNumericValue(token.charAt(i));
    }

    /**
     *
     * @return
     */
    public int sum() {
        int result = 0;
        int[] tokenIntArr = parse(token);
        for (int i = 0; i < tokenIntArr.length; i++) {
            result += tokenIntArr[i];
        }
        return result;
    }

    @Override
    public String toString() {
        return new String(token);
    }

    @Override
    public int compareTo(Token other) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than 
        // other and 0 if they are supposed to be equal
        String thisStringToken = new String(token);
        String otherStringToken = new String(other.getToken());

        return thisStringToken.compareTo(otherStringToken);
    }

}