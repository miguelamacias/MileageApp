package com.macisdev.mileageapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.utils.log
import com.macisdev.mileageapp.utils.showToast
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.io.File
import java.net.URLConnection

class SettingsFragment : PreferenceFragmentCompat() {
	private lateinit var roomBackup: RoomBackup
	private lateinit var resultLauncher: ActivityResultLauncher<Intent>
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			val restartIntent = Intent(requireContext(), MainActivity::class.java)

			restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			requireActivity().startActivity(restartIntent)
			if (context is Activity) {
				(context as Activity).finish()
			}
			Runtime.getRuntime().exit(0)
		}
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		val activity = activity as MainActivity
		roomBackup = activity.roomBackup

		preferenceManager.findPreference<Preference>("create_local_backup")?.setOnPreferenceClickListener {
			createLocalBackup()
			true
		}

		preferenceManager.findPreference<Preference>("restore_backup")?.setOnPreferenceClickListener {
			restoreBackup()
			true
		}

		preferenceManager.findPreference<Preference>("create_external_backup")?.setOnPreferenceClickListener {
			createExternalBackup()
			true
		}

		preferenceManager.findPreference<Preference>("contact_developer")?.setOnPreferenceClickListener {
			contactDeveloper()
			true
		}

		preferenceManager.findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
			openPrivacy()
			true
		}

		preferenceManager.findPreference<Preference>("terms_of_service")?.setOnPreferenceClickListener {
			openTerms()
			true
		}

		return super.onCreateView(inflater, container, savedInstanceState)
	}

	private fun createLocalBackup() {
		roomBackup
			.database(MileageRepository.get().database)
			.enableLogDebug(true)
			.customLogTag(MainActivity.TAG)
			.backupIsEncrypted(false)
			.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
			.apply {
				onCompleteListener { success, message, exitCode ->
					log("success: $success, message: $message, exitCode: $exitCode")
					if (success) {
						showToast(R.string.backup_created)

						view?.postDelayed(
							{
								restartApp(Intent(requireContext(), MainActivity::class.java))
							},
							1000
						)
					} else {
						showToast(R.string.backup_error)
					}
				}
			}
			.backup()
	}

	private fun restoreBackup() {
		roomBackup
			.database(MileageRepository.get().database)
			.enableLogDebug(true)
			.customLogTag(MainActivity.TAG)
			.backupIsEncrypted(false)
			.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
			.customRestoreDialogTitle(getString(R.string.restore_local_backup))
			.apply {
				onCompleteListener { success, message, exitCode ->
					log("success: $success, message: $message, exitCode: $exitCode")
					if (success) {
						showToast(R.string.backup_restored)

						view?.postDelayed(
							{
								restartApp(Intent(requireContext(), MainActivity::class.java))
							},
							1000
						)
					} else {
						showToast(R.string.backup_error)
					}
				}
			}
			.restore()
	}

	private fun createExternalBackup() {
		val backupFile = File(requireContext().cacheDir, "private_backup.db")

		roomBackup
			.database(MileageRepository.get().database)
			.enableLogDebug(true)
			.customLogTag(MainActivity.TAG)
			.backupIsEncrypted(false)
			.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE)
			.backupLocationCustomFile(backupFile)
			.apply {
				onCompleteListener { success, message, exitCode ->
					log("success: $success, message: $message, exitCode: $exitCode")
					if (success) {
						if (backupFile.exists()) {
							val fileUri = FileProvider.getUriForFile(
								requireContext().applicationContext,
								"com.macisdev.mileageapp.fileprovider", backupFile
							)

							val intentShareBackup = Intent(Intent.ACTION_SEND).apply {
								setDataAndType(
									fileUri,
									URLConnection.guessContentTypeFromName(backupFile.name)
								)

								addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
								putExtra(Intent.EXTRA_STREAM, fileUri)
								putExtra(
									Intent.EXTRA_SUBJECT,
									this@SettingsFragment.getString(R.string.external_backup_subject)
								)
								putExtra(
									Intent.EXTRA_TEXT,
									this@SettingsFragment.getString(R.string.external_backup_text)
								)
							}

							showToast(R.string.backup_created)

							resultLauncher.launch(Intent.createChooser(
								intentShareBackup,
								this@SettingsFragment.getString(R.string.save_backup)))
						}
					} else {
						showToast(R.string.backup_error)
					}
				}
			}
			.backup()
	}

	private fun contactDeveloper() {
		val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse("mailto:${getString(R.string.contact_email)}" +
					"?subject=${getString(R.string.app_name)} ${getString(R.string.app_version)}")
			putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.app_name)} ${getString(R.string.app_version)}")
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
		startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_developer)))
	}

	private fun openTerms() =
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_of_service_url))))

	private fun openPrivacy() =
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url))))
}