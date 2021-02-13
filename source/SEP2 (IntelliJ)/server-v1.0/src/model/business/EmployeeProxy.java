package model.business;

import model.Log;
import model.ServerManager;

import java.util.ArrayList;

/**responsible for managing a readers-writers lock pattern on an Employee instance*/
public class EmployeeProxy implements EmployeeEdit
{
  /**the real subject Employee instance*/
  private Employee realSubject;
  /**queue consisting of sessionIDs*/
  private ArrayList<Integer> queue;

  /**takes the real subject and initializes all instance variables*/
  EmployeeProxy(Employee employee)
  {
    realSubject = employee;
    queue = new ArrayList<>();
  }

  /**adds a session to the queue</br>
   * IMPORTANT: dependent on ModelManager's getCurrentSessionID() to get the session ID</br>
   * IMPORTANT: dependent on ModelManager's employeeAcquired() to notify the caller session if its already first in the queue*/
  void addToQueue()
  {
    int sessionID = ServerManager.get().getCurrentSessionID();
    if (!queue.contains(sessionID))
    {
      queue.add(sessionID);
      if (queue.get(0).equals(sessionID))
        ServerManager.get().employeeAcquired(sessionID, realSubject.getID());
    }
    else if (!queue.get(0).equals(sessionID))
    {
      Log.get().error(new IllegalStateException("session " + sessionID + " is already in the queue"));
    }
  }

  /**removes a session from the queue</br>
   * IMPORTANT: dependent on ModelManager's getCurrentSessionID()</br>
   * IMPORTANT: dependent on ModelManager's employeeAcquired() to notify the next session of the queue if the first one gets removed*/
  void removeFromQueue()
  {
    int sessionID = ServerManager.get().getCurrentSessionID();
    for (int i = 0; i < queue.size(); i++)
    {
      if (queue.get(i) == sessionID)
      {
        if (queue.size() > 1)
          if (queue.get(0) == sessionID)
            ServerManager.get().employeeAcquired(queue.get(1), realSubject.getID());
        queue.remove(i);
        break;
      }
    }
  }

  /**returns true if the Caller ClientHandler Thread is first in the queue meaning it has the lock and write privilege</br>
   * IMPORTANT: dependent on ModelManager's getCurrentSessionID()*/
  @Override public boolean isAcquired()
  {
    if (queue.size() == 0)
      return false;
    return queue.get(0).equals(ServerManager.get().getCurrentSessionID());
  }

  /**checks if the Caller ClientHandler Thread is first in the queue, and if yes, it delegates the request to the real subject</br>
   * IMPORTANT: dependent on ModelManager's getCurrentSessionID()*/
  @Override public void update(String name, int department, boolean admin)
  {
    int sessionID = ServerManager.get().getCurrentSessionID();
    if (queue.size() == 0)
      Log.get().error(new IllegalStateException("no write permission for session " + sessionID + " on employee " + realSubject.getID()));
    else
    {
      if (queue.get(0).equals(sessionID))
        realSubject.update(name, department, admin);
      else
        Log.get().error(new IllegalStateException("no write permission for session " + sessionID + " on employee " + realSubject.getID()));
    }
  }

  /**checks if the Caller ClientHandler Thread is first in the queue, and if yes, it delegates the request to the real subject</br>
   * IMPORTANT: dependent on ModelManager's getCurrentSessionID()*/
  @Override public void setPassword(String password)
  {
    int sessionID = ServerManager.get().getCurrentSessionID();
    if (queue.size() == 0)
      Log.get().error(new IllegalStateException("no write permission for session " + sessionID + " on employee " + realSubject.getID()));
    else
    {
      if (queue.get(0).equals(sessionID))
        realSubject.setPassword(password);
      else
        Log.get().error(new IllegalStateException("no write permission for session " + sessionID + " on employee " + realSubject.getID()));
    }
  }

  /**delegates the request to the real subject, since reading does not need a lock*/
  @Override public int getID()
  {
    return realSubject.getID();
  }

  /**delegates the request to the real subject, since reading does not need a lock*/
  @Override public boolean isArchived()
  {
    return realSubject.isArchived();
  }

  /**delegates the request to the real subject, since reading does not need a lock*/
  @Override public boolean isAdmin()
  {
    return realSubject.isAdmin();
  }

  /**delegates the request to the real subject, since reading does not need a lock*/
  @Override public String getName()
  {
    return realSubject.getName();
  }

  /**delegates the request to the real subject, since reading does not need a lock*/
  @Override public int getDepartment()
  {
    return realSubject.getDepartment();
  }

  /**delegates the request to the real subject, since reading does not need a lock*/
  @Override public boolean isPassword(String password)
  {
    return realSubject.isPassword(password);
  }
}
