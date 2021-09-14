package com.probase.probasepay.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
//import com.sf.encrypt.HibernateEncryptionInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
//import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtils
{
	private static SessionFactory sessionFactoryObject;
	
    static
    {
        try
        {
			
			try
			{
				/* Save the session factory that is built in this initializer*/
				//sessionFactoryObject = new Configuration().configure("SeamfixHibConfig.xml").buildSessionFactory();
				Configuration configuration=new Configuration();
				configuration.configure();
				ServiceRegistry sr= new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
				sessionFactoryObject=configuration.buildSessionFactory(sr);
			}
			catch (HibernateException ex)
			{
				throw new RuntimeException(ex);
			}
			
        }
        catch (HibernateException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static SessionFactory getSessionFactory() throws NamingException
    {
        return sessionFactoryObject;
    }
}