package com.uninum.elite.ui;

import java.io.File;
import java.util.Random;
import java.util.UUID;

import com.uninum.elite.R;
import com.uninum.elite.system.SystemManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class AddFile extends DialogFragment {
	
	ImageButton addImage, addVideo, camera, film;
	OnClickListener imageListener, videoListener, cameraListener, filmListener;
	String groupName, groupUUID, selfUUID;

	public static final String UPLOAD_FILE_URL = "http://130.211.246.100:8080/rest/v1/File/mediaObject/";
	private static final int REQUEST_PICK_IMAGE = 300;
	private static final int REQUEST_PICK_VIDEO = 400;
	private static final int REQUEST_TAKE_IMAGE = 500;
	private static final int REQUEST_TAKE_VIDEO = 600;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		imageListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getDialog().dismiss();
				Intent pickImageIntent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				Log.i("Jenny", "add file: action pick image");
				getActivity().startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
			}
			
		};
		
		videoListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getDialog().dismiss();
				Intent pickVideoIntent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
				getActivity().startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
			}
			
		};
		
		cameraListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getDialog().dismiss();
				String fileName = "elite_" + System.currentTimeMillis() + ".jpg";
				File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
				
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
				Message msg = new Message();
				msg.obj = fileName;
				Activity_Message.catchPhoto.sendMessage(msg);
				getActivity().startActivityForResult(intent, REQUEST_TAKE_IMAGE);
			}
			
		};
		
		filmListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getDialog().dismiss();
				String fileName = "Camera/elite_" + System.currentTimeMillis() + ".mp4";
				File video = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), fileName);
				
				Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(video));
				Message msg = new Message();
				msg.obj = fileName;
				Activity_Message.catchPhoto.sendMessage(msg);
				getActivity().startActivityForResult(intent, REQUEST_TAKE_VIDEO);
			}
			
		};
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View view = mInflater.inflate(R.layout.fragment_addfile, null);
		
		addImage = (ImageButton) view.findViewById(R.id.addfile_image);
		addVideo = (ImageButton) view.findViewById(R.id.addfile_video);
		camera = (ImageButton) view.findViewById(R.id.addfile_camera);
		film = (ImageButton) view.findViewById(R.id.addfile_takevideo);
		
		addImage.setOnClickListener(imageListener);
		addVideo.setOnClickListener(videoListener);
		camera.setOnClickListener(cameraListener);
		film.setOnClickListener(filmListener);
		
		return new AlertDialog.Builder(getActivity()).setView(view).create();
	}
	
}
