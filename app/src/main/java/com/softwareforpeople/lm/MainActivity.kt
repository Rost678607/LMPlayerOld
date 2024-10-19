package com.softwareforpeople.lm

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private fun replaceFragment(fragment: Fragment) { // метод замены фрагментов
        Log.d("Fragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ListFragment())
                .commit()
        }

        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        navigation.setOnItemSelectedListener { item ->   // обработка нажатий кнопок меню
            Log.d("Navigation", "Item selected: ${item.itemId}")
            when (item.itemId) {
                R.id.menu_list -> {
                    replaceFragment(ListFragment())
                    true
                }
                R.id.menu_play -> {
                    replaceFragment(PlayFragment())
                    true
                }
                R.id.menu_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
}

class ListFragment : Fragment() { // фрагмент списка терков

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_list, container, false)
    }
}

class PlayFragment : Fragment() { // фрагмент проигрывателя

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_play, container, false)
    }
}

class SettingsFragment : Fragment() { // фрагмент настроек

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_settings, container, false)
    }
}