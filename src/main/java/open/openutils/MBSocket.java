package open.openutils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sipgate.mp3wav.Converter;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
//import ws.schild.jave.Encoder;
//import ws.schild.jave.MultimediaObject;
//import ws.schild.jave.encode.AudioAttributes;
//import ws.schild.jave.encode.EncodingAttributes;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static open.openutils.OpenUtils.LOGGER;

public class MBSocket {
    private Socket socket;

    public Clip audioClip;
    private long resumeTime = 0;

    public void loadAudio(String urlString) {
        try {
            LOGGER.info("[OpenUtils] Fetching audio track.");
            // Fetching and converting
            URL url = new URL(urlString);
            InputStream audioInput = url.openStream();
            ByteArrayOutputStream wavOutput = new ByteArrayOutputStream();
            Converter converter = Converter.convertFrom(audioInput);
            converter.to(wavOutput);
            audioInput.close();
            InputStream audioInputStream = new ByteArrayInputStream(wavOutput.toByteArray());

            // Play
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioInputStream);
            audioInputStream.close();

            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);

            set_volume(MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS) * MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER));
            // this will only play it once, no loop, but hopefully that's fine :P

            audioStream.close();
        } catch (Exception e) {
            LOGGER.error("Failed to load audio for MB! " + e);
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§cCould not fetch the audio track for this round."));
        }
    }

    public void set_volume(double volume) {
        if (audioClip != null) {
            LOGGER.info("[OpenUtils] MB music volume set to " + volume + ".");
            FloatControl gainControl = ((FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN));

            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public void pause() {
        if (audioClip != null) {
            LOGGER.info("[OpenUtils] MB music paused.");
            resumeTime = audioClip.getMicrosecondPosition();
            audioClip.stop();
        }
    }

    public void resume() {
        if (audioClip != null) {
            LOGGER.info("[OpenUtils] MB music resumed.");
            audioClip.setMicrosecondPosition(resumeTime);
            audioClip.start();
        }
    }

    public void end() {
        if (audioClip != null) {
            LOGGER.info("[OpenUtils] MB music ended.");
            audioClip.stop();
            audioClip.close();
        }
        audioClip = null;
    }

    public void setupSocket(String playerName) {
        IO.Options options;

        Map<String, String> authMap = new HashMap<>();
        authMap.put("name", playerName);

        options = new IO.Options();
        options.auth = authMap;
        options.reconnection = true;

        try {
            socket = IO.socket("https://mbn.k55.se", options);
        } catch (URISyntaxException e) {
            LOGGER.error("Could not create MB socket. Autoplay in Musical Blocks will not work, please restart your game to fix the problem.");
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§cCould not create MB socket. Autoplay in Musical Blocks will not work, please restart your game to fix the problem."));
            return;
        }

        socket.on(Socket.EVENT_CONNECT, args -> System.out.println("Connected to SOCKET server"));

        socket.on("song", args -> {
            JsonElement arg = JsonParser.parseString(Arrays.toString(args));
            String musicTrack = arg.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
            resumeTime = 0; // important, wasted an hour on this :(
            loadAudio("https://antivpn.k55.se/tracks/" + musicTrack);
        });

        socket.on("play", args -> {
            resume();
        });

        socket.on("end", args -> {
            end();
        });

        socket.on("pause", args -> {
            pause();
        });
        socket.connect();
    }

    public void closeSocket() {
        LOGGER.info("Closing socket...");
        socket.disconnect();
    }
}
