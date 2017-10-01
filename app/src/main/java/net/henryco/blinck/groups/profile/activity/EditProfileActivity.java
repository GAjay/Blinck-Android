package net.henryco.blinck.groups.profile.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import lombok.val;
import net.henryco.blinck.BlinckApplication;
import net.henryco.blinck.R;
import net.henryco.blinck.service.MediaMainService;
import net.henryco.blinck.service.ProfileMainService;
import net.henryco.blinck.service.ProfileUpdateService;
import net.henryco.blinck.util.Authorization;
import net.henryco.blinck.util.form.user.UserProfileForm;
import net.henryco.blinck.util.reflect.AutoFind;
import net.henryco.blinck.util.reflect.AutoFinder;

import javax.inject.Inject;

public class EditProfileActivity extends AppCompatActivity {


	@AutoFind(R.id.image_avatar)
	private ImageView imageAvatar;

	@AutoFind(R.id.image_1)
	private ImageView image1;

	@AutoFind(R.id.image_2)
	private ImageView image2;

	@AutoFind(R.id.image_3)
	private ImageView image3;



	@Inject ProfileUpdateService profileUpdateService;
	@Inject ProfileMainService profileMainService;

	@Inject MediaMainService mediaMainService;



	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		AutoFinder.find(this);

		((BlinckApplication) getApplication()).getProfileComponent().inject(this);

		val authorization = Authorization.get(this);

		initActionBar();
		initImages(authorization);
		initProfile(authorization);
	}



	private void initActionBar() {

		final ActionBar supportActionBar = getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}
	}


	private void initImages(Authorization auth) {

		mediaMainService.loadProfileAvatarFromCache(this, auth,
				bitmap -> runOnUiThread(
						() -> imageAvatar.setImageBitmap(bitmap))
		);

		// TODO: 01/10/17
	}


	private void initProfile(Authorization auth) {

		UserProfileForm form = profileMainService.getProfileFromCache(auth.getUid());
		Log.d("Profile", form.toString());

		val firstName = (EditText) findViewById(R.id.first_name);
		firstName.setText(form.getUserName().getFirstName());

		val secondName = (EditText) findViewById(R.id.second_name);
		secondName.setText(ProfileHelper.safetyString(form.getUserName().getSecondName()));

		val lastName = (EditText) findViewById(R.id.last_name);
		lastName.setText(ProfileHelper.safetyString(form.getUserName().getLastName()));

		val about = (EditText) findViewById(R.id.about);
		about.setText(ProfileHelper.safetyString(form.getAbout()));

		initBirthdayField(form.getBirthday());
		initGenderField(form.getGender());
		initUsernameField(form.getUserName().getNickname());
	}



	private void initBirthdayField(Long date) {

		val birthday = (EditText) findViewById(R.id.birthday);
		birthday.setText(ProfileHelper.createBirthday(date));
		birthday.setOnClickListener(v -> {
			// TODO: 01/10/17 date picker dialog
		});
	}



	private void initGenderField(String gender) {

		if (gender.equalsIgnoreCase(ProfileHelper.GENDER_MALE))
			((RadioButton) findViewById(R.id.radio_male)).setChecked(true);

		else if (gender.equalsIgnoreCase(ProfileHelper.GENDER_FEMALE))
			((RadioButton) findViewById(R.id.radio_female)).setChecked(true);
	}



	private void initUsernameField(String username) {

		val nick = (EditText) findViewById(R.id.nickname);
		nick.setText(ProfileHelper.safetyString(username));

		nick.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}




	@Override
	public void onBackPressed() {

		// TODO: 30/09/17 UPLOAD AND SAVE CHANGES
		setResult(RESULT_OK);
		finish();
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

}