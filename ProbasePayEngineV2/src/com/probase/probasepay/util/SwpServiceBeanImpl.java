// license-header java merge-point
package com.probase.probasepay.util;


 // all the imports in the system -wyze
import java.util.List;

import	com.probase.probasepay.models.*;
//import hibernate
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.AliasToEntityMapResultTransformer;

/**
 * <p>
 * This is the bean implementation class
 * @author Onwuzu Onyekachi
 * @see SwpService object
 * @since  $date
 */
public class SwpServiceBeanImpl
    extends SwpServiceBean
    implements SwpService /*Implement the SwpService interface*/
{

//-----------------Internal crud methods Implementation--------------------
	
	//-----------------------General crud methods for the App ----------------------
	/**
	 * <p>
	 * This method creates a new Record. Irrespective of the Entity's Class. 
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param newRecord This is of type {@link java.lang.Object}
	 * @param aTrail This is of type {@link com.sf.audittrail.AuditTrail}
	 * @param org.hibernate.Session session
	 * @return {@link java.lang.Object}
	 * @see java.lang.Object 
	 * @since $date
	 * 
	 * 
	 * 
	 */
    protected java.lang.Object handleCreateNewRecord(org.hibernate.Session session, java.lang.Object newRecord)
	{
		session.save(newRecord);
		//session.save(aTrail);
		return newRecord;
	}
	
	/**
	 * <p>
	 * This method updates an existing Record. Irrespective of the Entity's Class.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param existingRecord This is of type {@link java.lang.Object}
	 * @param aTrail This is of type {@link com.sf.audittrail.AuditTrail}
	 * @param org.hibernate.Session session
	 * @return void
	 * @since $date
	 * 
	 * 
	 * 
	 */
    protected void handleUpdateRecord(org.hibernate.Session session, java.lang.Object existingRecord)
	{	
		session.saveOrUpdate(existingRecord);
		//session.save(aTrail);
	}
	
	/**
	 * <p>
	 * This method deletes an existing Record. Irrespective of the Entity's Class.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param existingRecord This is of type {@link java.lang.Object}
	 * @param aTrail This is of type {@link com.sf.audittrail.AuditTrail}
	 * @param org.hibernate.Session session
	 * @return void
	 * @since $date
	 * 
	 * 
	 * 
	 */
    protected void handleDeleteRecord(org.hibernate.Session session, java.lang.Object existingRecord)
	{		
		session.delete(existingRecord);
		//session.save(aTrail);
	}
	
	/**
	  * <p>
	 * This method returns an object of the specified by clazz paramter using the supplied id.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param clazz This is of type {@link java.lang.Class}
	 * @param recordId This is of type {@link java.lang.Long}
	 * @param org.hibernate.Session session
	 * @return {@link java.lang.Object}
	 * @see java.lang.Object
	 * @since $date
	 * 
	 * 
	 * 
	 */
    protected java.lang.Object handleGetRecordById(org.hibernate.Session session, java.lang.Class clazz, java.lang.Long recordId)
	{
		return session.get(clazz, recordId);
	}
	
	/**
	 * <p>
	 * This method returns collection of the records, specified by the clazz parameter.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param clazz This is of type {@link java.lang.Class}
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @see java.util.Collection
	 * @since $date
	 * 
	 * 
	 * 
	 */
    protected java.util.Collection handleGetAllRecords(org.hibernate.Session session, java.lang.Class clazz)
	{
		java.lang.String hql = "SELECT u FROM " + clazz.getName() + " u";
        org.hibernate.Query query = session.createQuery(hql);
		java.util.Collection returnCol = (java.util.Collection)query.list();
		return returnCol;
	}	
	
	/**this method returns collection of the records, specified by the clazz parameter that is within the pageIndex and pageSize. It throws an exception if there is a problem*/
	/**
	 * <p>
	 * This method returns collection of the records, specified by the clazz parameter that is within the pageIndex and pageSize.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param clazz This is of type {@link java.lang.Class}
	 * @param pageIndex This is of type {@link int}
	 * @param pageSize This is of type {@link int}
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @see java.util.Collection 
	 * @since $date
	 * 
	 * 
	 * 
	 */
    protected java.util.Collection handleGetAllRecords(org.hibernate.Session session, java.lang.Class clazz, int pageIndex, int pageSize)
	{
		java.lang.String hql = "SELECT u FROM " + clazz.getName() + " u";
        org.hibernate.Query query = session.createQuery(hql);
		query.setFirstResult(pageIndex);
        query.setMaxResults(pageSize);
		java.util.Collection returnCol = (java.util.Collection)query.list();
		return returnCol;
	}
	
	/* ------------------------- (section added by Charles Ofoefule) -------------------------------- */
    //--------Generic Methods; used by parsing HQL -----------//
 /**
	 * <p>
	 * The method used to retrieve a unique result using the hibernate query passed to it. 
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param hql This is of type {@link java.lang.String}
	 * @param org.hibernate.Session session
	 * @return {@link java.lang.Object}
	 * @see java.lang.Object
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.lang.Object handleGetUniqueRecordByHQL(org.hibernate.Session session, java.lang.String hql)
	{
		org.hibernate.Query query = session.createQuery(hql);
		return query.uniqueResult();
	}
	
	/**
	 * <p>
	 * The method used to get a collection of objects using the hibernate query passes as an argument. 
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param hql This is of type {@link java.lang.String}
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @see java.util.Collection
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.util.Collection handleGetAllRecordsByHQL(org.hibernate.Session session, java.lang.String hql)
	{
		org.hibernate.Query query = session.createQuery(hql);
		java.util.Collection returnCol = (java.util.Collection)query.list();
		return returnCol;
	}
	
	/**
	 * <p>
	 * The method used to get a paginated collection of all objects using the hibernate query passed as an argument. 
	 * This is necessary for panination things. i.e. using same query, you can return results in paged format
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param hql This is of type {@link java.lang.String}
	 * @param pageIndex This is of type {@link int}
	 * @param pageSize This is of type {@link int}
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @see java.util.Collection
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.util.Collection handleGetAllRecordsByHQL(org.hibernate.Session session, java.lang.String hql, int pageIndex, int pageSize)
	{
		org.hibernate.Query query = session.createQuery(hql);
		query.setFirstResult(pageIndex);
        query.setMaxResults(pageSize);
		java.util.Collection returnCol = (java.util.Collection)query.list();
		return returnCol;
	}
	
	//--------Generic Methods; used by parsing Class names and paremeter Ids -----------//
	//solves the bottle neck of many to one relationship -- all methods take pagination into account
	
	/**
	 * <p>
	 * The method used to get a paginated collection of a result set based on a many-to-one relationship with another entity. 
	 * for a single many to one relationship
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param manyClass This is of type {@link java.lang.Class}
	 * @param oneClass This is of type {@link java.lang.Class}
	 * @param parameterId This is of type {@link java.lang.Long}
	 * @param pageIndex This is of type {@link int}
	 * @param pageSize This is of type {@link int}
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @see java.util.Collection
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.util.Collection handleGetAllRecordsByParameterId(org.hibernate.Session session, java.lang.Class manyClass, java.lang.Class oneClass, java.lang.Long parameterId, int pageIndex, int pageSize){
		java.lang.String strOneClass = getProcessedClassName(oneClass);
		java.lang.String hql = "SELECT c FROM " + manyClass.getName() + " c WHERE c." + strOneClass + ".id=" + parameterId;
        org.hibernate.Query query = session.createQuery(hql);
		query.setFirstResult(pageIndex);
        query.setMaxResults(pageSize);
		java.util.Collection returnCol = (java.util.Collection)query.list();
		return returnCol;
	}
	
	/**
	 * <p>
	 * The method used to get a paginated collection of a result set based on a many-to-one relationship between two entities.
	 * The argument {@code strOperator} is used to determine if its either an OR or an AND. 
	 * For a many to one relationship where there are two CMR
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param manyClass This is of type {@link java.lang.Class}
	 * @param firstOneClass This is of type {@link java.lang.Class}
	 * @param secondOneClass This is of type {@link java.lang.Class}
	 * @param firstParameterId This is of type {@link java.lang.Long}
	 * @param secondParameterId This is of type {@link java.lang.Long}
	 * @param strOperator This is of type {@link java.lang.String} . It could either be OR or AND
	 * @param pageIndex This is of type {@link int}
	 * @param pageSize This is of type {@link int}
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @see java.util.Collection
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.util.Collection handleGetAllRecordsByTwoParameterIds(org.hibernate.Session session, java.lang.Class manyClass, java.lang.Class firstOneClass, java.lang.Class secondOneClass, java.lang.Long firstParameterId, java.lang.Long secondParameterId, java.lang.String strOperator, int pageIndex, int pageSize){
		if(strOperator != null && (!strOperator.equalsIgnoreCase("and") || !strOperator.equalsIgnoreCase("or"))){
			strOperator = "AND"; //set a default value
		}
		java.lang.String strFirstOneClass = getProcessedClassName(firstOneClass);
		java.lang.String strSecondOneClass = getProcessedClassName(secondOneClass);
		
		java.lang.String hql = "SELECT DISTINCT c FROM " + manyClass.getName() + " c WHERE c." + strFirstOneClass + ".id=" + firstParameterId +  " " + strOperator + " c." + strSecondOneClass + ".id=" +secondParameterId;
        org.hibernate.Query query = session.createQuery(hql);
		query.setFirstResult(pageIndex);
        query.setMaxResults(pageSize);
		java.util.Collection returnCol = (java.util.Collection)query.list();
		return returnCol;
		
	}
	//Overloaded create, update and delete (Useful for swing impl and impl without user action and audit trail requirement)
	/**
	 * <p>
	 * This method creates a new Record. Irrespective of the Entity's Class. 
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param newRecord This is of type {@link java.lang.Object}
	 * @param org.hibernate.Session session
	 * @return {@link java.lang.Object}
	 * @see java.lang.Object 
	 * @since $date
	 * 
	 * 
	 * 
	 */
	 
    /*protected java.lang.Object handleCreateNewRecord(org.hibernate.Session session, java.lang.Object newRecord)
	{
		session.save(newRecord);
		return newRecord;
	}*/
	
	/**
	 * <p>
	 * This method updates an existing Record. Irrespective of the Entity's Class.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param existingRecord This is of type {@link java.lang.Object}
	 * @param org.hibernate.Session session
	 * @return void
	 * @since $date
	 * 
	 * 
	 * 
	 */
    /*protected void handleUpdateRecord(org.hibernate.Session session, java.lang.Object existingRecord)
	{	
		session.saveOrUpdate(existingRecord);
	}*/
	
	/**
	 * <p>
	 * This method deletes an existing Record. Irrespective of the Entity's Class.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param existingRecord This is of type {@link java.lang.Object}
	 * @param org.hibernate.Session session
	 * @return void
	 * @since $date
	 * 
	 * 
	 * 
	 */
    /*protected void handleDeleteRecord(org.hibernate.Session session, java.lang.Object existingRecord)
	{		
		session.delete(existingRecord);
	}*/
	//------auxilliary method to get a Criteria Object (needed by developer who will want to use criteria in place of HQL)------- //
	/**
	 * <p>
	 * Auxilliary method to get a Criteria Object
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param clazz This is of type {@link java.lang.Class}
	 * @param aTrail This is of type {@link com.sf.audittrail.AuditTrail}
	 * @param org.hibernate.Session session
	 * @return org.hibernate.Criteria
	 * @since $date
	 * 
	 * 
	 * 
	 */
	 protected org.hibernate.Criteria handleGetCriteriaObject(org.hibernate.Session session, java.lang.Class clazz)
	{		
		return session.createCriteria(clazz);
	}
	
	/**
	 * <p>
	 * This method returns a value object; accepts a criteria object as a parameter.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param org.hibernate.Criteria criteria
	 * @param org.hibernate.Session session
	 * @return java.lang.Object
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.lang.Object handleGetUniqueRecordByCriteria(org.hibernate.Session session, org.hibernate.Criteria criteria)
	{
		return criteria.uniqueResult();
	}
	
	/**
	 * <p>
	 * This method returns a collection of value objects; accepts a criteria object as a parameter.
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param org.hibernate.Criteria criteria
	 * @param org.hibernate.Session session
	 * @return {@link java.util.Collection}
	 * @since $date
	 * 
	 * 
	 * 
	 */
	protected java.util.Collection handleGetAllRecordsByCriteria(org.hibernate.Session session, org.hibernate.Criteria criteria)
	{
		java.util.Collection returnCol = (java.util.Collection)criteria.list();
		return returnCol;
	}
	
	
	//----- helper method (Charles Ofoefule)-----//
	/**
	 * <p>
	 * Helper method processes the class name to get a caramel coding convention
	 * 
	 * @author Onwuzu Onyekachi
	 * 
	 * @param clazz This is of type {@link java.lang.Class}
	 * @return java.lang.String
	 * @see java.lang.String
	 * @since $date
	 * 
	 * 
	 * 
	 */
	private java.lang.String getProcessedClassName(java.lang.Class clazz){
		java.lang.String retString = clazz + "";
		retString = retString.substring(retString.lastIndexOf("."));
		
        retString = retString.substring(1, 2).toLowerCase() + "" + retString.substring(2);
		return retString;
	}


	@Override
	protected List handleGetQueryBySQLResults(Session session, String sql) {
		// TODO Auto-generated method stub
		Query q= session.createSQLQuery(sql);
		q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		List list = q.list();
		return list;
       
	}


	@Override
	protected List handleGetQueryBySQLResultsWithKeys(Session session, String sql) {
		// TODO Auto-generated method stub
		Query q= session.createSQLQuery(sql);
		q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		List list = q.list();
		return list;
       
	}


	@Override
	protected int handleInsertIntoDb(Session session, String sql) {
		// TODO Auto-generated method stub
		Query query = session.createQuery(sql);
		int result = query.executeUpdate();
		return result;
       
	}

	
	//--------Generic Methods; useful for search Operations used by parsing the attribute name and attribute values -----------//
	//TODO: add more codes here to perform generic level search by virtue of criteria 
	
	


}