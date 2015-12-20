package com.uninum.elite.utility;

import java.io.File;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.FrameRecorder;

public class VideoHandler {
	
	private FFmpegFrameGrabber grabber;
	private FrameRecorder recorder;
	private Frame frame;
	
	public VideoHandler(File videoFile) {
		
		try {
			
			grabber = new FFmpegFrameGrabber(videoFile);
			grabber.start();
			
			recorder = new FFmpegFrameRecorder("ORIGINAL_FILE.mp4", grabber.getImageHeight(), grabber.getImageWidth() , grabber.getAudioChannels());
			recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
	        recorder.setFrameRate(grabber.getFrameRate()<30.0?grabber.getFrameRate():30.0);
	        recorder.setSampleFormat(grabber.getSampleFormat());
	        recorder.start();
			
		} catch (Exception | org.bytedeco.javacv.FrameRecorder.Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
