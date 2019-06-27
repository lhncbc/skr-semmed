/*
 * WARNING: DO NOT EDIT THIS FILE. This is a generated file that is synchronized
 * by MyEclipse Hibernate tool integration.
 *
 * Created Thu Mar 23 16:12:49 EST 2006 by MyEclipse Hibernate Tool.
 */
package gov.nih.nlm.semmed.model;

import java.io.Serializable;

/**
 * A class that represents a row in the PREDICATION_ARGUMENT table. 
 * You can customize the behavior of this class by editing the class, {@link PredicationArgument()}.
 * WARNING: DO NOT EDIT THIS FILE. This is a generated file that is synchronized
 * by MyEclipse Hibernate tool integration.
 */
public abstract class AbstractPredicationArgument 
    implements Serializable
{
    /** The cached hash code value for this instance.  Settting to 0 triggers re-calculation. */
    private int hashValue = 0;

    /** The composite primary key value. */
    private java.lang.Long predicationArgumentId;

    /** The value of the predication association. */
    private Predication predication;

    /** The value of the conceptSemtype association. */
    private ConceptSemtype conceptSemtype;

    /** The value of the simple type property. */
    private java.lang.String type;

    /**
     * Simple constructor of AbstractPredicationArgument instances.
     */
    public AbstractPredicationArgument()
    {
    }

    /**
     * Constructor of AbstractPredicationArgument instances given a simple primary key.
     * @param predicationArgumentId
     */
    public AbstractPredicationArgument(java.lang.Long predicationArgumentId)
    {
        this.setPredicationArgumentId(predicationArgumentId);
    }

    /**
     * Return the simple primary key value that identifies this object.
     * @return java.lang.Long
     */
    public java.lang.Long getPredicationArgumentId()
    {
        return predicationArgumentId;
    }

    /**
     * Set the simple primary key value that identifies this object.
     * @param predicationArgumentId
     */
    public void setPredicationArgumentId(java.lang.Long predicationArgumentId)
    {
        this.hashValue = 0;
        this.predicationArgumentId = predicationArgumentId;
    }

    /**
     * Return the value of the PREDICATION_ID column.
     * @return Predication
     */
    public Predication getPredication()
    {
        return this.predication;
    }

    /**
     * Set the value of the PREDICATION_ID column.
     * @param predication
     */
    public void setPredication(Predication predication)
    {
        this.predication = predication;
    }

    /**
     * Return the value of the CONCEPT_SEMTYPE_ID column.
     * @return ConceptSemtype
     */
    public ConceptSemtype getConceptSemtype()
    {
        return this.conceptSemtype;
    }

    /**
     * Set the value of the CONCEPT_SEMTYPE_ID column.
     * @param conceptSemtype
     */
    public void setConceptSemtype(ConceptSemtype conceptSemtype)
    {
        this.conceptSemtype = conceptSemtype;
    }

    /**
     * Return the value of the TYPE column.
     * @return java.lang.String
     */
    public java.lang.String getType()
    {
        return this.type;
    }

    /**
     * Set the value of the TYPE column.
     * @param type
     */
    public void setType(java.lang.String type)
    {
        this.type = type;
    }

    /**
     * Implementation of the equals comparison on the basis of equality of the primary key values.
     * @param rhs
     * @return boolean
     */
    public boolean equals(Object rhs)
    {
        if (rhs == null)
            return false;
        if (! (rhs instanceof PredicationArgument))
            return false;
        PredicationArgument that = (PredicationArgument) rhs;
        if (this.getPredicationArgumentId() == null || that.getPredicationArgumentId() == null)
            return false;
        return (this.getPredicationArgumentId().equals(that.getPredicationArgumentId()));
    }

    /**
     * Implementation of the hashCode method conforming to the Bloch pattern with
     * the exception of array properties (these are very unlikely primary key types).
     * @return int
     */
    public int hashCode()
    {
        if (this.hashValue == 0)
        {
            int result = 17;
            int predicationArgumentIdValue = this.getPredicationArgumentId() == null ? 0 : this.getPredicationArgumentId().hashCode();
            result = result * 37 + predicationArgumentIdValue;
            this.hashValue = result;
        }
        return this.hashValue;
    }
}