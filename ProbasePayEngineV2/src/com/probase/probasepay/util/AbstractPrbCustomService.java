package com.probase.probasepay.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


public class AbstractPrbCustomService {

  private static SessionFactory sessionFactory = null;

  private static Logger log = Logger.getLogger(AbstractPrbCustomService.class);

  static {
    try { sessionFactory = HibernateUtils.getSessionFactory();
    } catch (Exception e)
    {
    	System.out.println(e.getMessage());
    }
  }
  
  public static void setSessionFactory(SessionFactory sessionFactory)
  {
	  AbstractPrbCustomService.sessionFactory = sessionFactory;  
  }

  public <T> T executeQueryUniqueResult(String query, Object param) {
    List paramList = new ArrayList();
    paramList.add(param);
    return executeQueryUniqueResult(query, paramList);
  }

  public <T> T executeQueryUniqueResult(String query, List paramsList) {
	Session session = getSession();
    Query q = session.createQuery(query);
    for (int i = 0; i < paramsList.size(); i++) {
      q.setParameter(i, paramsList.get(i));
    }
    Object c = q.uniqueResult();
    System.out.println("Close connection for session id = " + session.hashCode());
    return (T)c;
  }

  public <T> List<T> executeQuery(String query, Object param) {
    List paramList = new ArrayList();
    List entityList = new ArrayList();
    paramList.add(param);
    entityList =  executeQuery(query, paramList);
    return entityList;
  }

  public <T> List<T> executeQuery(String query, Object param, int start, int size)
  {
    List paramList = new ArrayList();
    paramList.add(param);
    List entityList = new ArrayList();
    entityList = executeQuery(query, paramList, start, size);
    return entityList;
  }

  public <T> List<T> executeQuery(String query, List paramsList) {
    List entityList = new ArrayList();
    Session session = getSession();
    try {
      Query q = session.createQuery(query);
      for (int i = 0; i < paramsList.size(); i++) {
        q.setParameter(i, paramsList.get(i));
      }
      entityList = q.list();
      System.out.println("Close connection for session id = " + session.hashCode());
    } catch (Exception ex) {
      log.error("", ex);
    }
    return entityList;
  }

  public <T> List<T> executeQuery(String query, List paramsList, int start, int size)
  {
    List entityList = new ArrayList();
    Session session = getSession();
    try {
      Query q = session.createQuery(query);
      for (int i = 0; i < paramsList.size(); i++) {
        q.setParameter(i, paramsList.get(i));
      }
      q.setFirstResult(start);
      q.setMaxResults(size);
      entityList = q.list();
      System.out.println("Close connection for session id = " + session.hashCode());
    } catch (Exception ex) {
      log.error("", ex);
    }
    return entityList;
  }

  public <T> List<T> executeQuery(String query, Map<String, Object> paramsMap) {
    List entityList = new ArrayList();
    Session session = getSession();
    try {
      Query q = session.createQuery(query);
      for (String key : paramsMap.keySet()) {
        q.setParameter(key, paramsMap.get(key));
      }
      entityList = q.list();
      System.out.println("Close connection for session id = " + session.hashCode());
    } catch (Exception ex) {
      log.error("", ex);
    }
    return entityList;
  }

  public <T> List<T> executeQuery(String query) {
    List entityList = new ArrayList();
    Session session = getSession();
    try {
      Query q = session.createQuery(query);
      entityList = q.list();
      System.out.println("Close connection for session id = " + session.hashCode());
    } catch (Exception ex) {
      log.error("", ex);
    }
    return entityList;
  }

  public <T> List<T> executeQuery(String query, Map<String, Object> paramsMap, int start, int size)
  {
    List entityList = new ArrayList();
    Session session = getSession();
    try {
      Query q = session.createQuery(query);
      for (String key : paramsMap.keySet()) {
        q.setParameter(key, paramsMap.get(key));
      }
      q.setFirstResult(start);
      q.setMaxResults(size);
      entityList = q.list();
      System.out.println("Close connection for session id = " + session.hashCode());
    } catch (Exception ex) {
      log.error("", ex);
    }
    return entityList;
  }
  
  
  public java.lang.Object createNewRecord(java.lang.Object object)
  {
	  Session session = null;
	  Transaction tx = null;
      try{			
			session = getSession();			
			//tx = session.beginTransaction();
			java.lang.Object newRecord = session.save(object);
			//tx.commit();
			session.close();
			System.out.println("Close connection for session id = " + session.hashCode());
			return newRecord;
	  }catch(Exception e)
	  {
		  session.close();
		  System.out.println("Close connection for session id = " + session.hashCode());
		  System.out.println(e.getMessage());
		  return null;
	  }finally
      {
        	if (session != null)
			{
	      		try
					{
	      			session.close();
	      			System.out.println("Close connection for session id = " + session.hashCode());
	      		}catch (HibernateException he)
	      		{
	      			System.out.println(he.getMessage());
	            }
			}
      }
  }
  
  
  public void updateRecord(Object object)
  {
	  Session session = null;
	  Transaction tx = null;
      try{			
			session = getSession();			
			//tx = session.beginTransaction();
			session.saveOrUpdate(object);
			//tx.commit();
			session.close();
			System.out.println("Close connection for session id = " + session.hashCode());
	  }catch(Exception e)
	  {
		  System.out.println(e.getMessage());
	  }finally
      {
          	if (session != null)
			{
	      		try
					{
	      			session.close();
	      			System.out.println("Close connection for session id = " + session.hashCode());
	      		}catch (HibernateException he)
	      		{
	      			System.out.println(he.getMessage());
	            }
			}
      }
  }
  
  public void deleteRecord(Object object)
  {
	  Session session = null;
	  Transaction tx = null;
      try{			
			session = getSession();			
			//tx = session.beginTransaction();
			session.delete(object);
			//tx.commit();
			session.close();
			System.out.println("Close connection for session id = " + session.hashCode());
	  }catch(Exception e)
	  {
		  System.out.println(e.getMessage());
	  }finally
      {
        	if (session != null)
			{
	      		try
					{
	      			session.close();
	      			System.out.println("Close connection for session id = " + session.hashCode());
	      		}catch (HibernateException he)
	      		{
	      			System.out.println(he.getMessage());
	            }
			}
      }
  }
  
  

  public Session getSession() {
    Session session = null;
    
    session= sessionFactory.openSession();
    System.out.println("Open connection for session id = " + session.hashCode());
    return session;
  }
}
