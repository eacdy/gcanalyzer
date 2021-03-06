package net.milanaleksic.gcanalyzer

import groovy.io.FileType
import java.awt.Dimension
import java.awt.Point
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import net.milanaleksic.gcanalyzer.util.Utils
import javax.swing.*
import net.milanaleksic.gcanalyzer.gui.*

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:09 AM
 */
class GCAnalyzerApplication implements ParsingFinishedListener {

    public static final int STACK_TRACE_MAX_LENGTH = 2048

    private static final String TITLE = 'GC Analyzer - created by Milan Aleksic (www.milanaleksic.net)'

    private AtomicInteger counter = new AtomicInteger(0)

    private JFrame frame

    public static void main(String[] args) {
        new GCAnalyzerApplication().exec(args)
    }

    private def exec(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler({ Thread t, Throwable e ->
            def stackTrace = Utils.getSmallerStackTraceForThrowable(e)
            if (stackTrace && stackTrace.size() > STACK_TRACE_MAX_LENGTH)
                stackTrace = stackTrace.substring(0, STACK_TRACE_MAX_LENGTH) + "..."
            e.printStackTrace()
            JOptionPane.showMessageDialog(null, "Exception occurred: ${e.getMessage()}\r\n\r\nDetails:\r\n${stackTrace}")
        } as UncaughtExceptionHandler)

        AbstractGCAnalyzerController.setUpNimbusLookAndFeel()

        AbstractGCAnalyzerController controller = null
        SwingUtilities.invokeAndWait {
            frame = new JFrame(TITLE)

            controller = new ApplicationGCAnalyzerController(frame, this)

            frame.setPreferredSize(new Dimension(750, 550))
            frame.setLocation(new Point(100, 100))
            frame.pack()
            frame.setExtendedState((int) frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
            frame.setVisible(true)
        }
        if (controller)
            startRequestedFilesParsing(args, controller)
    }

    public void startRequestedFilesParsing(args, analyzer) {
        if (!args || args.size() == 0)
            return
        new Thread({
            def files = []
            args.each { String filename ->
                def file = new File(filename)
                if (file.isDirectory()) {
                    file.eachFile(FileType.FILES) { File childFile ->
                        files << childFile.absolutePath
                    }
                }
                else
                    files << filename
            }
            if (files.size() > 0) {
                frame.setTitle("$TITLE - Parsing input")
                counter.set(files.size())
                files.each {
                    analyzer.addFile(it)
                }
            }
        } as Runnable).run()
    }

    @Override
    void onFileParsingFinished(String fileName) {
        if (counter.decrementAndGet() == 0) {
            frame.setTitle(TITLE)
        }
    }

}
