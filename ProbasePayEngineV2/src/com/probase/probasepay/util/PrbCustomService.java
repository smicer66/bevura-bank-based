package com.probase.probasepay.util;


import org.apache.log4j.Logger;

import com.probase.probasepay.models.*;




public class PrbCustomService extends AbstractPrbCustomService{

  private static Logger log = Logger.getLogger(PrbCustomService.class);

  public static PrbCustomService getInstance()
  {
    return new PrbCustomService();
  }

  /*public PortalUser getPortalUserByOrbitaId(String orbitaId) {
    //String query = "select pu from PortalUser pu where pu.userId=?";
	String query = "select pu from PortalUser pu where pu.userId= '" + orbitaId + "'"; 
	System.out.println(query);
    List paramsList = new ArrayList();
    return (PortalUser)executeQueryUniqueResult(query, paramsList);
  }

  public List<Role> getPortalUserRoleType(long userOrbitaId) {
    String id = String.valueOf(userOrbitaId);
    log.debug("User Orbita ID: " + id);
    String query = "select pur.role from PortalUserRole pur where pur.portalUser.userId=?";
    List params = new ArrayList();
    params.add(id);

    return executeQuery(query, id);
  }*/
  
  
  
  
  

  /*public Set<RoleType> getRoles(Long userOrbitaId) {
    log.debug("User Orbita Id is: " + userOrbitaId);
    List<Role> roles = getPortalUserRoleType(userOrbitaId.longValue());
    Set roleTypes = new HashSet();

    if ((roles != null) && (!roles.isEmpty())) {
      for (Role role : roles) {
        roleTypes.add(role.get);
      }
    }
    return roleTypes;
  }*/

  
}