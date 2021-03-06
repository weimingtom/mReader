// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: http://svn.code.sf.net/p/htmlparser/code/trunk/parser/src/main/java/org/htmlparser/sax/Attributes.java $
// $Author: derrickoswald $
// $Date: 2011-04-25 17:39:12 +0800 (周一, 2011-04-25) $
// $Revision: 74 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the Common Public License; either
// version 1.0 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// Common Public License for more details.
//
// You should have received a copy of the Common Public License
// along with this library; if not, the license is available from
// the Open Source Initiative (OSI) website:
//   http://opensource.org/licenses/cpl1.0.php

package org.htmlparser.sax;

import java.util.Vector;

import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Provides access to the tag attributes.
 */
public class Attributes
    implements
        org.xml.sax.Attributes
{
    /**
     * The tag from which attributes are exposed.
     */
    protected Tag mTag;
    
    /**
     * The utility class that converts namespaces.
     */
    protected NamespaceSupport mSupport;

    /**
     * Elements of the qname.
     * Allocated once for all uses of {@link #mSupport}.
     */
    protected String[] mParts;

    /**
     * The non-whitespace attributes.
     */
    protected Vector<Attribute> mAttributes;

    /**
     * Create an attibute access object.
     * @param tag The tag to expose.
     * @param support The namespace converter.
     * @param parts The elements of the qualified name.
     */
    public Attributes (Tag tag, NamespaceSupport support, String[] parts)
    {
        mTag = tag;
        mSupport = support;
        mParts = parts;
        mAttributes = null;
    }

    protected Vector<Attribute> getAttributes ()
    {
        Vector<Attribute> attributes;
        int size;
        Attribute attribute;
        String string;

        if (null == mAttributes)
        {
            mAttributes = new Vector<Attribute> ();
            attributes = mTag.getAttributesEx ();
            if (null != attributes)
            {
                size = attributes.size ();
                for (int i = 1; i < size; i++)
                {
                    attribute = attributes.elementAt (i);
                    string = attribute.getName ();
                    if (null != string) // not whitespace
                        mAttributes.add (attribute);
                }
            }
        }

        return (mAttributes);
    }

    ////////////////////////////////////////////////////////////////////
    // Indexed access.
    ////////////////////////////////////////////////////////////////////


    /**
     * Return the number of attributes in the list.
     *
     * <p>Once you know the number of attributes, you can iterate
     * through the list.</p>
     *
     * @return The number of attributes in the list.
     * @see #getURI(int)
     * @see #getLocalName(int)
     * @see #getQName(int)
     * @see #getType(int)
     * @see #getValue(int)
     */
    public int getLength ()
    {
        return (getAttributes ().size ());
    }


    /**
     * Look up an attribute's Namespace URI by index.
     *
     * @param index The attribute index (zero-based).
     * @return The Namespace URI, or the empty string if none
     *         is available, or null if the index is out of
     *         range.
     * @see #getLength
     */
    public String getURI (int index)
    {
        mSupport.processName (getQName (index), mParts, true);
        return (mParts[0]);
    }


    /**
     * Look up an attribute's local name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The local name, or the empty string if Namespace
     *         processing is not being performed, or null
     *         if the index is out of range.
     * @see #getLength
     */
    public String getLocalName (int index)
    {
        mSupport.processName (getQName (index), mParts, true);
        return (mParts[1]);
    }


    /**
     * Look up an attribute's XML qualified (prefixed) name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The XML qualified name, or the empty string
     *         if none is available, or null if the index
     *         is out of range.
     * @see #getLength
     */
    public String getQName (int index)
    {
        Attribute attribute;
        String ret;
        
        attribute = getAttributes ().elementAt (index);
        ret = attribute.getName ();
        
        return (ret);
    }

    /**
     * Look up an attribute's type by index.
     *
     * <p>The attribute type is one of the strings "CDATA", "ID",
     * "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES",
     * or "NOTATION" (always in upper case).</p>
     *
     * <p>If the parser has not read a declaration for the attribute,
     * or if the parser does not report attribute types, then it must
     * return the value "CDATA" as stated in the XML 1.0 Recommendation
     * (clause 3.3.3, "Attribute-Value Normalization").</p>
     *
     * <p>For an enumerated attribute that is not a notation, the
     * parser will report the type as "NMTOKEN".</p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's type as a string, or null if the
     *         index is out of range.
     * @see #getLength
     */
    public String getType (int index)
    {
        return ("CDATA");
    }


    /**
     * Look up an attribute's value by index.
     *
     * <p>If the attribute value is a list of tokens (IDREFS,
     * ENTITIES, or NMTOKENS), the tokens will be concatenated
     * into a single string with each token separated by a
     * single space.</p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's value as a string, or null if the
     *         index is out of range.
     * @see #getLength
     */
    public String getValue (int index)
    {
        Attribute attribute;
        String ret;
        
        attribute = getAttributes ().elementAt (index);
        ret = attribute.getValue ();
        if (null == ret)
            ret = "";

        return (ret);
    }


    ////////////////////////////////////////////////////////////////////
    // Name-based query.
    ////////////////////////////////////////////////////////////////////


    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param uri The Namespace URI, or the empty string if
     *        the name has no Namespace URI.
     * @param localName The attribute's local name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    public int getIndex (String uri, String localName)
    {
        Vector<Attribute> attributes;
        int size;
        Attribute attribute;
        String string;
        int ret;

        ret = -1;

        attributes = getAttributes ();
        size = attributes.size ();
        for (int i = 1; i < size; i++)
        {
            attribute = attributes.elementAt (i);
            string = attribute.getName ();
            mSupport.processName (string, mParts, true);
            if (  uri.equals (mParts[0])
                && localName.equalsIgnoreCase (mParts[1]))
            {
                ret = i;
                i = size; // exit fast
            }
        }

        return (ret);
    }


    /**
     * Look up the index of an attribute by XML qualified (prefixed) name.
     *
     * @param qName The qualified (prefixed) name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    public int getIndex (String qName)
    {
        mSupport.processName (qName, mParts, true);
        return (getIndex (mParts[0], mParts[1]));
    }


    /**
     * Look up an attribute's type by Namespace name.
     *
     * <p>See {@link #getType(int) getType(int)} for a description
     * of the possible types.</p>
     *
     * @param uri The Namespace URI, or the empty String if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if Namespace
     *         processing is not being performed.
     */
    public String getType (String uri, String localName)
    {
        return (null);
    }


    /**
     * Look up an attribute's type by XML qualified (prefixed) name.
     *
     * <p>See {@link #getType(int) getType(int)} for a description
     * of the possible types.</p>
     *
     * @param qName The XML qualified name.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if qualified names
     *         are not available.
     */
    public String getType (String qName)
    {
        return (null);
    }


    /**
     * Look up an attribute's value by Namespace name.
     *
     * <p>See {@link #getValue(int) getValue(int)} for a description
     * of the possible values.</p>
     *
     * @param uri The Namespace URI, or the empty String if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list.
     */
    public String getValue (String uri, String localName)
    {
        int index;
        String ret;

        ret = null;

        index = getIndex (uri, localName);
        if (-1 != index)
            ret = getValue (index);

        return (ret);
    }


    /**
     * Look up an attribute's value by XML qualified (prefixed) name.
     *
     * <p>See {@link #getValue(int) getValue(int)} for a description
     * of the possible values.</p>
     *
     * @param qName The XML qualified name.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list or if qualified names
     *         are not available.
     */
    public String getValue (String qName)
    {
        mSupport.processName (qName, mParts, true);
        return (getValue (mParts[0], mParts[1]));
    }
}
