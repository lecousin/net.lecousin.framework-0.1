package net.lecousin.framework.ui.eclipse.dialog;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class ErrorDlg {

    public static void error(String title, String message, Throwable e) {
        MessageDialog.openError(MyDialog.getModalShell(), title, message + "\r\n" + e.getMessage());
    }

    public static void error(String title, String message) {
        MessageDialog.openError(MyDialog.getModalShell(), title, message);
    }

    public static void exception(String title, String message, String pluginID, Throwable e) {
        IStatus status = buildStatus(e, pluginID);
        IStatus logStatus = buildLogStatus(e, pluginID);
        Platform.getLog(Platform.getBundle(pluginID)).log(logStatus);
        class ShowError implements Runnable {
            public ShowError(String title, String message, IStatus status)
            { this.title = title; this.message = message; this.status = status; }
            private String title, message;
            private IStatus status;
            public void run() {
                ErrorDialog.openError(null, title, message, status);
            }
        }
        PlatformUI.getWorkbench().getDisplay().syncExec(new ShowError(title, message, status));
    }

    public static void multi_errors(String title, String message, String pluginID, Collection<String> errors) {
        MultiStatus status = buildMultiErrorsStatus(message, errors, pluginID);
        //ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message, status);
        ErrorDialog.openError(null, title, message, status);
    }
    
    public static void multi_exceptions(String title, String message, String pluginID, Collection<? extends Throwable> exceptions) {
        MultiStatus status = buildMultiExceptionsStatus(message, exceptions, pluginID);
        ErrorDialog.openError(null, title, message, status);
    }
    
    public static MultiStatus buildMultiErrorsStatus(String message, Collection<String> errors, String pluginID) {
        MultiStatus status = new MultiStatus(pluginID, 1, message, null);
        for (Iterator<String> it = errors.iterator(); it.hasNext(); ) {
            String s = it.next();
            if (s == null) s = "#NULL#";
            status.add(new Status(IStatus.ERROR, pluginID, 1, s, null));
        }
        return status;
    }
    public static MultiStatus buildMultiExceptionsStatus(String message, Collection<? extends Throwable> exceptions, String pluginID) {
        MultiStatus status = new MultiStatus(pluginID, 1, message, null);
        for (Throwable t : exceptions)
            status.add(buildStatus(t, pluginID));
        return status;
    }
    
    public static IStatus buildStatus(Throwable e, String pluginID) {
        if (e instanceof CoreException)
            return ((CoreException)e).getStatus();
        String msg = e.getMessage();
        if (msg == null) msg = e.getClass().getName();
        IStatus status = new MultiStatus(pluginID, 1, msg, e);
        StackTraceElement[] trace = e.getStackTrace();
        if (trace != null)
            for (int i = 0; i < trace.length; ++i)
                ((MultiStatus)status).add(new Status(IStatus.ERROR, pluginID, 1, trace[i].getClassName() + "#" + trace[i].getMethodName() + ":" + trace[i].getLineNumber(), null));
        return status;
    }
    public static IStatus buildLogStatus(Throwable e, String pluginID) {
        if (e instanceof CoreException)
            return ((CoreException)e).getStatus();
        String msg = e.getMessage();
        if (msg == null) msg = e.getClass().getName();
        String log = msg + "\r\n";
        StackTraceElement[] trace = e.getStackTrace();
        if (trace != null)
            for (int i = 0; i < trace.length; ++i)
                log += "  " + trace[i].getClassName() + "#" + trace[i].getMethodName() + ":" + trace[i].getLineNumber() + "\r\n";
        return new Status(IStatus.ERROR, pluginID, 1, log, e);
    }
    
    public static void logError(String message, Throwable t, String pluginID) {
        Platform.getLog(Platform.getBundle(pluginID)).log(new Status(IStatus.ERROR, pluginID, 1, message, t));
    }
}
