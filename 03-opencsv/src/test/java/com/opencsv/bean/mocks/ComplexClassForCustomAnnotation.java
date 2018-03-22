/*
 * Copyright 2016 Andrew Rucker Jones.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opencsv.bean.mocks;

/**
 * The members of this class are not annotated, but the entire class is
 * intended to be annotated in a containing class.
 *
 * @author Andrew Rucker Jones
 */
public class ComplexClassForCustomAnnotation {
    public int i;
    public char c;
    public String s;
    
    public int getI() {return i;} public void setI(int i) {this.i = i;}
    public char getC() {return c;} public void setC(char c) {this.c = c;}
    public String getS() {return s;} public void setS(String s) {this.s = s;}
}
