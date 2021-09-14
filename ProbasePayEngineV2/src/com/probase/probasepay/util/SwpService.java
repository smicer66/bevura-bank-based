package com.probase.probasepay.util;

// all the imports in the system -wyze
import java.util.List;

import org.hibernate.Session;

import	com.probase.probasepay.models.*;

/**
 * This is the service class
 * @author Onwuzu Onyekachi
 * @see smartpay.service.SwpService object
 * @since  $date
 */
public interface SwpService
    //extends javax.ejb.EJBLocalObject
{	//-----------------internal crud methods --------------------
	
     //upgraded by Charles Ofoefule
	//-----------------------General crud methods for all Entity----------------------
	/**this method creates a new Record. Irrespective of the Entity's Class. It throws an exception if there is a problem*/
    public java.lang.Object createNewRecord(java.lang.Object newRecord) throws Exception;
	
	/**this method updates an existing Record. Irrespective of the Entity's Class. It throws an exception if there is a problem*/
    public void updateRecord(java.lang.Object existingRecord) throws Exception;
	
	/**this method deletes an existing Record. Irrespective of the Entity's Class. It throws an exception if there is a problem*/
    public void deleteRecord(java.lang.Object existingRecord) throws Exception;
	
	/**this method returns an object of the specified by clazz paramter using the supplied id. It throws an exception if there is a problem*/
    public java.lang.Object getRecordById(java.lang.Class clazz, java.lang.Long recordId) throws Exception;
	
	/**this method returns collection of the records, specified by the clazz parameter. It throws an exception if there is a problem*/
    public java.util.Collection getAllRecords(java.lang.Class clazz) throws Exception;	
	
	/**this method returns collection of the records, specified by the clazz parameter that is within the pageIndex and pageSize. It throws an exception if there is a problem*/
    public java.util.Collection getAllRecords(java.lang.Class clazz, int pageIndex, int pageSize) throws Exception;	
	

	//improved KP (Key Performance) methods for developers
	/**this method returns a unique object; accepts hql string as parameter*/
    public java.lang.Object getUniqueRecordByHQL(java.lang.String hql) throws Exception;
	
	/**this method returns a collection of objects; accepts hql string as parameter*/
    public java.util.Collection getAllRecordsByHQL(java.lang.String hql) throws Exception;
	
	/**this method returns a collection of objects; accepts hql string as parameter*/
    public java.util.Collection getAllRecordsByHQL(java.lang.String hql, int pageIndex, int pageSize) throws Exception;

	/**this method returns a collection of objects from a many to one mapping;*/
    public java.util.Collection getAllRecordsByParameterId(java.lang.Class manyClass, java.lang.Class oneClass, java.lang.Long parameterId, int pageIndex, int pageSize) throws Exception;
	
	/**this method returns a collection of objects from a many to one mapping; where the many object has two CMRs*/
    public java.util.Collection getAllRecordsByTwoParameterIds(java.lang.Class manyClass, java.lang.Class firstOneClass, java.lang.Class secondOneClass, java.lang.Long firstParameterId, java.lang.Long secondParameterId, java.lang.String strOperator, int pageIndex, int pageSize) throws Exception;
	
	//Overloaded create, update and delete (Useful for swing impl and impl without user action and audit trail requirement)
	/**this method creates a new Record. Irrespective of the Entity's Class. It throws an exception if there is a problem
    public java.lang.Object createNewRecord(java.lang.Object newRecord);*/
	
	/**this method updates an existing Record. Irrespective of the Entity's Class. It throws an exception if there is a problem
    public void updateRecord(java.lang.Object existingRecord);*/
	
	/**this method deletes an existing Record. Irrespective of the Entity's Class. It throws an exception if there is a problem
    public void deleteRecord(java.lang.Object existingRecord);*/
	
	//auxiliary method to get a Criteria object
	public org.hibernate.Criteria getCriteriaObject(java.lang.Class clazz) throws Exception;
	
	//methods to return results using criteria as the parameter
	/**this method returns a unique object; acceptscriteria object as parameter*/
    public java.lang.Object getUniqueRecordByCriteria(org.hibernate.Criteria criteria) throws Exception;
	
	/**this method returns a collection of objects; accepts criteria object as parameter*/
    public java.util.Collection getAllRecordsByCriteria(org.hibernate.Criteria criteria) throws Exception;
	
    public org.hibernate.Transaction getStartTransaction() throws Exception;
	
    public void getCommitTransaction(org.hibernate.Transaction tx) throws Exception;
	
    public void getRollBackTransaction(org.hibernate.Transaction tx) throws Exception;
    
    
    public java.util.List getQueryBySQLResults(String sql) throws Exception;
    
    public java.util.List getQueryBySQLResultsWithKeys(String sql) throws Exception;
    
    public int insertIntoDb(String sql) throws Exception;
	
	
    // ---------------- business methods  ----------------------
}