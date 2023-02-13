package engine.gif;

import engine.IO;
import engine.Image;
import engine.Modifier;
import engine.color.ColorBuffer;
import engine.util.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static engine.IO.*;

public class GIFRecorder
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    private final GIFEncoder encoder = new GIFEncoder();
    
    private boolean recording;
    private long    lastFrame;
    private int     frameCounter;
    private String  timestamp;
    
    public void update()
    {
        IO.Event event = keyboardOnKeyDown();
        if (event.fired() && !event.consumed() && modifierOnly(Modifier.CONTROL, Modifier.SHIFT))
        {
            if (this.recording)
            {
                stopRecording();
            }
            else
            {
                startRecording();
            }
            event.consume();
        }
    }
    
    public void draw()
    {
        if (this.recording)
        {
            final int GIF_RECORD_FRAMERATE = 10;
            this.frameCounter++;
        
            // NOTE: We record one gif frame every 10 game frames
            if ((this.frameCounter % GIF_RECORD_FRAMERATE) == 0)
            {
                long time  = System.nanoTime();
                long delta = time - this.lastFrame;
                this.lastFrame = time;
            
                // Get image data for the current frame (from back buffer)
                // NOTE: This process is quite slow... :(
    
                int width  = windowFramebufferSize().x();
                int height = windowFramebufferSize().y();
            
                //ColorBuffer data  = GLState.readFrontBuffer(0, 0, width, height); // TODO
                ColorBuffer data  = ColorBuffer.malloc(width * height);
                Image       image = new Image(data, width, height);
                //
                boolean result = this.encoder.addFrame(image, (int) (delta / 1_000_000));
            
                if (!result) GIFRecorder.LOGGER.warning("Could not add frame to", this.timestamp);
            
                image.delete(); // Free image data
            }
        }
    }
    
    public void startRecording()
    {
        if (this.recording) return;
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH.mm.ss"));
        
        this.recording    = true;
        this.lastFrame    = System.nanoTime();
        this.frameCounter = 0;
        this.timestamp    = String.format("Recording - %s.gif", timestamp);
    
        int width  = windowFramebufferSize().x();
        int height = windowFramebufferSize().y();
        
        boolean result = this.encoder.start(this.timestamp, width, height);
        
        if (result)
        {
            GIFRecorder.LOGGER.info("Started GIF Recording: %s", this.timestamp);
        }
        else
        {
            GIFRecorder.LOGGER.warning("Could not start GIF recording");
        }
    }
    
    public void stopRecording()
    {
        if (!this.recording) return;
        
        this.recording = false;
        
        boolean result = this.encoder.finish();
    
        GIFRecorder.LOGGER.info("Finished GIF Recording. Result:", result ? "Success" : "Failure");
    }
}
