/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.css;

import java.util.Collections;
import java.util.List;
import javafx.scene.Node;

/**
 * A CssMetaData instance provides information about the CSS style and
 * provides the hooks that allow CSS to set a property value. 
 * It encapsulates the CSS property name, the type into which the CSS value 
 * is converted, and the default value of the property. 
 * <p>
 * CssMetaData is the bridge between a value that can be represented 
 * syntactically in a .css file, and a {@link StyleableProperty}. There is 
 * a one-to-one correspondence between a CssMetaData and a StyleableProperty.
 * Typically, the CssMetaData of a will include the CssMetaData of its ancestors. 
 * During CSS processing, the CSS engine iterates over the Node's CssMetaData, 
 * looks up the parsed value of each, converts the parsed value, and 
 * sets the value on the StyleableProperty. 
 * <p>
 * The method {@link Node#getCssMetaData()} is called to obtain the 
 * List&lt;CssMetaData&gt;. This method is called frequently and it is prudent
 * to return a static list rather than creating the list on each call. By 
 * convention, node classes that have CssMetaData will implement a
 * static method {@code getClassCssMetaData()} and it is customary to have
 * {@code getCssMetaData()} simply return {@code getClassCssMetaData()}. The
 * purpose of {@code getClassCssMetaData()} is to allow sub-classes to easily
 * include the CssMetaData of some ancestor.
 * <p>
 * This example is a typical implementation. 
 * <code><pre>
 * private DoubleProperty gapProperty = new StyleableDoubleProperty(0) {
 *     {@literal @}Override 
 *      public CssMetaData getCssMetaData() {
 *          return GAP_META_DATA;
 *      }
 *
 *      {@literal @}Override
 *      public Object getBean() {
 *          return MyWidget.this;
 *      }
 *
 *      {@literal @}Override
 *      public String getName() {
 *          return "gap";
 *      }
 * };
 * 
 * private static final CssMetaData GAP_META_DATA = 
 *     new CssMetaData<MyWidget, Number>("-my-gap", StyleConverter.getSizeConverter(), 0d) {
 * 
 *        {@literal @}Override
 *        public boolean isSettable(MyWidget node) {
 *            return node.gapProperty == null || !node.gapProperty.isBound();
 *        }
 *    
 *        {@literal @}Override
 *        public StyleableProperty<Number> getStyleableProperty(MyWidget node) {
 *            return node.gapProperty;
 *        }
 * };
 * 
 * private static final List<CssMetaData> cssMetaDataList; 
 * static {
 *     List<CssMetaData> temp = new ArrayList<CssMetaData>(Control.getClassCssMetaData());
 *     Collections.addAll(temp, GAP_META_DATA);
 *     cssMetaDataList = Collections.unmodifiableList(temp);
 * }
 * 
 * public static List<CssMetaData> getClassCssMetaData() {
 *     return cssMetaDataList;
 * }
 * 
 * {@literal @}Override
 * public List<CssMetaData> getCssMetaData() {
 *     return getClassCssMetaData();
 * }
 * </pre><code>
 * @param <N> The type of Node
 * @param <V> The type into which the parsed value is converted. 
 */
public abstract class CssMetaData<N extends Node, V> {
    
    /**
     * Set the value of the corresponding property on the given Node.
     * @param node The node on which the property value is being set
     * @param value The value to which the property is set
     */
    public void set(N node, V value, StyleOrigin origin) {
        
        final StyleableProperty<V> styleableProperty = getStyleableProperty(node);
        final StyleOrigin currentOrigin = styleableProperty.getStyleOrigin();  
        final V currentValue = styleableProperty.getValue();
        
        // RT-21185: Only apply the style if something has changed. 
        if ((currentOrigin != origin)
            || (currentValue != null 
                ? currentValue.equals(value) == false 
                : value != null)) {
            
            styleableProperty.applyStyle(origin, value);
            
        }
    }

    /**
     * Check to see if the corresponding property on the given Node is
     * settable. This method is called before any styles are looked up for the
     * given property. It is abstract so that the code can check if the property 
     * is settable without expanding the property. Generally, the property is 
     * settable if it is not null or is not bound. 
     * 
     * @param node The node on which the property value is being set
     * @return true if the property can be set.
     */
    public abstract boolean isSettable(N node);

    /**
     * Return the corresponding {@link StyleableProperty} for the given Node. 
     * Note that calling this method will cause the property to be expanded. 
     * @param node
     * @return 
     */
    public abstract StyleableProperty<V> getStyleableProperty(N node);
            
    private final String property;
    /**
     * @return the CSS property name
     */
    public final String getProperty() {
        return property;
    }

    private final StyleConverter converter;
    /**
     * @return The CSS converter that handles conversion from a CSS value to a Java Object
     */
    public final StyleConverter getConverter() {
        return converter;
    }

    private final V initialValue;
    /**
     * The initial value of a CssMetaData corresponds to the default 
     * value of the StyleableProperty in code. 
     * For example, the default value of Shape.fill is Color.BLACK and the
     * initialValue of Shape.StyleableProperties.FILL is also Color.BLACK.
     * <p>
     * There may be exceptions to this, however. The initialValue may depend
     * on the state of the Node. A ScrollBar has a default orientation of 
     * horizontal. If the ScrollBar is vertical, however, this method should
     * return Orientation.VERTICAL. Otherwise, a vertical ScrollBar would be
     * incorrectly set to a horizontal ScrollBar when the initial value is 
     * applied.
     * @return The initial value of the property, possibly null
     */
    public V getInitialValue(N node) {
        return initialValue;
    }
    
    private final List<CssMetaData> subProperties;
    /**
     * The sub-properties refers to the constituent properties of this property,
     * if any. For example, "-fx-font-weight" is sub-property of "-fx-font".
     */
    public final List<CssMetaData> getSubProperties() {
        return subProperties;
    }

    private final boolean inherits;
    /**
     * If true, the value of this property is the same as
     * the parent's computed value of this property.
     * @default false
     * @see <a href="http://www.w3.org/TR/css3-cascade/#inheritance">CSS Inheritance</a>
     */
    public final boolean isInherits() {
        return inherits;
    }

    /**
     * Construct a CssMetaData with the given parameters and no sub-properties.
     * @param property the CSS property
     * @param converter the StyleConverter used to convert the CSS parsed value to a Java object.
     * @param initalValue the CSS string 
     * @param inherits true if this property uses CSS inheritance
     * @param subProperties the sub-properties of this property. For example,
     * the -fx-font property has the sub-properties -fx-font-family, 
     * -fx-font-size, -fx-font-weight, and -fx-font-style.
     */
    protected CssMetaData(
            final String property, 
            final StyleConverter converter, 
            final V initialValue, 
            boolean inherits, 
            final List<CssMetaData> subProperties) {        
        
        this.property = property;
        this.converter = converter;
        this.initialValue = initialValue;
        this.inherits = inherits;
        this.subProperties = subProperties != null ? Collections.unmodifiableList(subProperties) : null;        
        
        if (this.property == null || this.converter == null) 
            throw new IllegalArgumentException("neither property nor converter can be null");
    }

    /**
     * Construct a CssMetaData with the given parameters and no sub-properties.
     * @param property the CSS property
     * @param converter the StyleConverter used to convert the CSS parsed value to a Java object.
     * @param initalValue the CSS string 
     * @param inherits true if this property uses CSS inheritance
     */
    protected CssMetaData(
            final String property, 
            final StyleConverter converter, 
            final V initialValue, 
            boolean inherits) {
        this(property, converter, initialValue, inherits, null);
    }

    /**
     * Construct a CssMetaData with the given parameters, inherit set to
     * false and no sub-properties.
     * @param property the CSS property
     * @param converter the StyleConverter used to convert the CSS parsed value to a Java object.
     * @param initalValue the CSS string 
     */
    protected CssMetaData(
            final String property, 
            final StyleConverter converter, 
            final V initialValue) {
        this(property, converter, initialValue, false, null);
    }

    /**
     * Construct a CssMetaData with the given parameters, initialValue is
     * null, inherit is set to false, and no sub-properties.
     * @param property the CSS property
     * @param converter the StyleConverter used to convert the CSS parsed value to a Java object.
     * @param initalValue the CSS string 
     */
    protected CssMetaData(
            final String property, 
            final StyleConverter converter) {
        this(property, converter, null, false, null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CssMetaData<N, V> other = (CssMetaData<N, V>) obj;
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.property != null ? this.property.hashCode() : 0);
        return hash;
    }
    
    
    @Override public String toString() {
        return  new StringBuilder("CSSProperty {")
            .append("property: ").append(property)
            .append(", converter: ").append(converter.toString())
            .append(", initalValue: ").append(String.valueOf(initialValue))
            .append(", inherits: ").append(inherits)
            .append(", subProperties: ").append(
                (subProperties != null) ? subProperties.toString() : "[]")
            .append("}").toString();
    }


}