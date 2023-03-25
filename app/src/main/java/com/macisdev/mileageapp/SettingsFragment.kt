package com.macisdev.mileageapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.utils.Constants
import com.macisdev.mileageapp.utils.Utils
import com.macisdev.mileageapp.utils.log
import com.macisdev.mileageapp.utils.showToast
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import java.io.File
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
	private lateinit var roomBackup: RoomBackup
	private lateinit var resultLauncher: ActivityResultLauncher<Intent>
	private lateinit var preferences: SharedPreferences
	private var fuelServiceActivated: Boolean = true

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
		preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		fuelServiceActivated = preferences.getBoolean("fuel_service_activate", false)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		val activity = activity as MainActivity
		roomBackup = activity.roomBackup

		val currentPreferredStation =
			preferences.getString(Constants.PREFERRED_GAS_STATION_NAME, getString(R.string.no_preferred_station))

		changeFuelServicePrefVisibility(fuelServiceActivated)

		preferenceManager.findPreference<Preference>("choose_fuel_station")?.summary =
			getString(R.string.preferred_station, currentPreferredStation)

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

		preferenceManager.findPreference<Preference>("choose_fuel_station")?.setOnPreferenceClickListener {
			chooseFuelStation()
			true
		}

		preferenceManager.findPreference<SwitchPreference>("fuel_service_activate")
			?.setOnPreferenceChangeListener { _, newValue ->
			changeFuelServicePrefVisibility(newValue as Boolean)
			true
		}

		return super.onCreateView(inflater, container, savedInstanceState)
	}

	private fun changeFuelServicePrefVisibility(visible: Boolean) {
		val fuelStationPreference: Preference? = findPreference("choose_fuel_station")
		fuelStationPreference?.isVisible = visible

		val fuelTypePreference: Preference? = findPreference("fuel_type_preference")
		fuelTypePreference?.isVisible = visible

		val fuelDiscountPreference: Preference? = findPreference("fuel_service_discount")
		fuelDiscountPreference?.isVisible = visible
	}

	private fun createLocalBackup() {
		roomBackup
			.database(AppDataRepository.get().database)
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
			.database(AppDataRepository.get().database)
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
		val backupFileName = "backup_mileage_" + Utils.formatDate(Date())
			.replace('/', '.').plus(".db")

		val backupFile = File(requireContext().cacheDir, backupFileName)

		roomBackup
			.database(AppDataRepository.get().database)
			.enableLogDebug(true)
			.customLogTag(MainActivity.TAG)
			.backupIsEncrypted(false)
			.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE)
			.backupLocationCustomFile(backupFile)
			.apply {
				onCompleteListener { success, message, exitCode ->
					log("success: $success, message: $message, exitCode: $exitCode")
					if (success) {
						val shareBackupIntent = Utils.getShareFileIntent(
							this@SettingsFragment.requireContext().applicationContext,
							backupFile,
							R.string.external_backup_subject,
							R.string.external_backup_text,
							R.string.save_backup
						)

						shareBackupIntent?.let { resultLauncher.launch(it) }

						showToast(R.string.backup_created)

					} else {
						showToast(R.string.backup_error)
					}
				}
			}
			.backup()
	}

	private fun contactDeveloper() {
		val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse(
				"mailto:${getString(R.string.contact_email)}" +
						"?subject=${getString(R.string.app_name)} ${getString(R.string.app_version)}"
			)
			putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.app_name)} ${getString(R.string.app_version)}")
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
		startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_developer)))
	}

	private fun openTerms() =
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_of_service_url))))

	private fun openPrivacy() =
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url))))

	private fun chooseFuelStation() {
		val directions = SettingsFragmentDirections.actionSettingsFragmentToFuelStationsFragment()
		findNavController().navigate(directions)
	}
}