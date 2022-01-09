package nick.template.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import dagger.hilt.android.AndroidEntryPoint
import nick.template.databinding.MainActivityBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportFragmentManager.findFragmentById(binding.fragmentContainer.id) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<MainFragment>(binding.fragmentContainer.id)
            }
        }
    }
}
