/**
 * ExecuteScriptAction.java
 *
 *
 * Created: Wed Dec 23 15:22:01 1998
 *
 * @author
 * @version
 */

package tn5250j.scripting;

import tn5250j.SessionPanel;
import tn5250j.tools.logging.TN5250jLogFactory;
import tn5250j.tools.logging.TN5250jLogger;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExecuteScriptAction extends AbstractAction {
	
	private static final long serialVersionUID = 181938308216785668L;
	private static final transient TN5250jLogger LOG = TN5250jLogFactory.getLogger(ExecuteScriptAction.class);
	
	private String _scriptFile;
	private SessionPanel ses;

   public ExecuteScriptAction(String name, String scriptFile, SessionPanel session) {
      super(name);
      _scriptFile = scriptFile;
      ses = session;
   }

   public void actionPerformed(ActionEvent e) {
	   if (LOG.isDebugEnabled()) {
		   LOG.debug("Invoking " + _scriptFile);
	   }

      try {
         InterpreterDriverManager.executeScriptFile(ses,_scriptFile);
      }
      catch (InterpreterDriver.InterpreterException ex) {
         ses.setMacroRunning(false);
         System.out.println(ex);
         ex.printStackTrace();
      }
   }
}
