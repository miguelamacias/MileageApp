package com.macisdev.mileageapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

							view?.postDelayed(
								{
									restartApp(
										Intent.createChooser(
											intentShareBackup,
											this@SettingsFragment.getString(R.string.save_backup)
										)
									)
								}, 1000
							)
						}
					} else {
						showToast(R.string.backup_error)
					}
				}
			}
			.backup()
	}

	private fun contactDeveloper() {
		showToast("Not implemented yet!")
	}
}