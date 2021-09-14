package com.probase.probasepay.event;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;



import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import com.probase.probasepay.models.Merchant;

public class HibernateEventListener implements PreInsertEventListener, PostDeleteEventListener, PostInsertEventListener, PreUpdateEventListener, PostUpdateEventListener
{
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostUpdate(PostUpdateEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		// TODO Auto-generated method stub
		Object entity = event.getEntity();
		if(entity.getClass().equals(Merchant.class))
		{
			Merchant merchant = (Merchant)entity;
			merchant.setUpdated_at(new Date());
		}
		return true;
	}

	@Override
	public void onPostInsert(PostInsertEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostDelete(PostDeleteEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		// TODO Auto-generated method stub
		Object entity = event.getEntity();
		System.out.println("1onPreInsert --> " + entity.getClass().getName());
		if(entity.getClass().equals(Merchant.class))
		{
			System.out.println("onPreInsert --> ");
			Merchant merchant = (Merchant)entity;
			merchant.setCreated_at(new Date());
		}
		return true;
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
