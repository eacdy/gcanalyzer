package net.milanaleksic.gcanalyzer

import java.applet.Applet
import java.awt.Font
import javax.swing.SwingUtilities
import javax.swing.JLabel

class GCAnalyzerApplet extends Applet implements FileParsingFinishedListener {

    private GCAnalyzer analyzer

    @Override
    void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    analyzer = new GCAnalyzer(fileParsingFinishedListener:GCAnalyzerApplet.this)
                    analyzer.initGuiForApplet(GCAnalyzerApplet.this)
                }
            })
            loadLog(getLogLocation())
        } catch (Exception e) {
            showVeryBigErrorMessage("Problem: ${e.getMessage()}")
            e.printStackTrace()
        }
    }

    private URL getLogLocation() {
        def logParameter = getParameter('completeLogUrl')
        if (logParameter)
            return new URL(logParameter)

        logParameter = getParameter('logUrl')
        if (!logParameter)
            throw new RuntimeException("Neither 'logUrl' nor 'completeLogUrl' parameter was sent to applet - so no graphs to show. Sorry!")

        URL targetUrl = new URL(getCodeBase(), logParameter)
        return targetUrl
    }

    private def showMessage(message) {
        println message
        showStatus(message)
    }

    private def loadLog(URL logUrl) {
        showMessage("Loading GC log from $logUrl")
        analyzer.addUrl(logUrl)
    }

    private def showVeryBigErrorMessage(String message) {
        JLabel label = new JLabel(message)
        Font font = new Font("Arial", Font.BOLD, 14)
        label.setFont(font)
        add(label)
    }

    @Override
    void onFileParsingFinished(String fileName) {
        showStatus("")
    }

}
