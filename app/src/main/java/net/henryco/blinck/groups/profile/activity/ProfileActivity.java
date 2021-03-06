package net.henryco.blinck.groups.profile.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import net.henryco.blinck.BlinckApplication;
import net.henryco.blinck.R;
import net.henryco.blinck.service.MediaMainService;
import net.henryco.blinck.service.ProfileMainService;
import net.henryco.blinck.util.Authorization;
import net.henryco.blinck.util.TimeUtils;
import net.henryco.blinck.util.form.user.UserProfileForm;
import net.henryco.blinck.util.reflect.AutoFind;
import net.henryco.blinck.util.reflect.AutoFinder;
import net.henryco.blinck.util.task.VoidAsyncTask;

import javax.inject.Inject;

public class ProfileActivity extends AppCompatActivity {

	private static final int CODE_ACTIVITY_EDIT = 2;


	@AutoFind(R.id.toolbar)
	private Toolbar toolbar;

	@AutoFind(R.id.fab)
	private FloatingActionButton fab;

	@AutoFind(R.id.toolbar_layout)
	private CollapsingToolbarLayout collapsingToolbarLayout;

	@AutoFind(R.id.toolbar_image)
	private ImageView toolbarImage;


	@Inject ProfileMainService profileMainService;
	@Inject MediaMainService mediaMainService;



	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		AutoFinder.find(this);

		((BlinckApplication) getApplication()).getProfileComponent().inject(this);

		initActionBar();
		initFab();

		initUserProfile();
	}



	private void initActionBar() {

		setSupportActionBar(toolbar);
		ActionBar supportActionBar = getSupportActionBar();

		if (supportActionBar != null)
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		collapsingToolbarLayout.setVisibility(View.INVISIBLE);
		collapsingToolbarLayout.setEnabled(false);
	}



	private void initFab() {

		fab.setOnClickListener(v -> startActivityForResult(
				new Intent(this, EditProfileActivity.class),
				CODE_ACTIVITY_EDIT
		));
		fab.setEnabled(false);
	}



	private void initUserProfile() {

		Authorization auth = Authorization.get(this);

		new VoidAsyncTask(() -> {

			profileMainService.loadAndCacheProfileFromServer(auth, this::loadProfileData);
			mediaMainService.loadAndCacheProfileAvatarImage(this, auth, this::loadProfileAvatar);
		}).execute();
	}



	private void loadProfileData(UserProfileForm profile) {

		final String name = profile.getUserName().getFirstName();
		final int age = TimeUtils.calculateAgeFromBirthDate(profile.getBirthday());

		final String gender = ProfileHelper.createGender(profile.getGender());
		final String birthday = ProfileHelper.createBirthday(profile.getBirthday());
		final String nameFull = ProfileHelper.createName(
				profile.getUserName().getFirstName(),
				profile.getUserName().getSecondName(),
				profile.getUserName().getLastName()
		);

		final String about = profile.getAbout();
		final String nickname = profile.getUserName().getNickname();


		runOnUiThread(() -> {

			collapsingToolbarLayout.setTitle(ProfileHelper.createTitle(name, age));
			collapsingToolbarLayout.setVisibility(View.VISIBLE);
			collapsingToolbarLayout.setEnabled(true);
			fab.setEnabled(true);

			((TextView) findViewById(R.id.text_name)).setText(nameFull);
			((TextView) findViewById(R.id.text_gender)).setText(gender);
			((TextView) findViewById(R.id.text_birthday)).setText(birthday);

			if (about != null && !about.isEmpty())
				((TextView) findViewById(R.id.text_about)).setText(about);

			if (nickname!= null && !nickname.isEmpty()) {
				((TextView) findViewById(R.id.text_username)).setText(nickname);

				findViewById(R.id.container_username)
						.setVisibility(View.VISIBLE);
			}

			findViewById(R.id.container_profile)
					.setVisibility(View.VISIBLE);
		});
	}



	private void loadProfileAvatar(Bitmap bitmap) {

		runOnUiThread(() -> {

			toolbarImage.setImageBitmap(bitmap);
			toolbarImage.setOnClickListener(v -> {
				// TODO: 30/09/17
			});
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CODE_ACTIVITY_EDIT)
			if (resultCode == RESULT_OK) initUserProfile();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}

}


