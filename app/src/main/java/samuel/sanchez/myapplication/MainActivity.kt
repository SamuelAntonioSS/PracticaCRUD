package samuel.sanchez.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import modelo.ClaseConexion

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //1-mandar a llamar todos los elemtos de la vista
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtPrecio = findViewById<EditText>(R.id.txtPrecio)
        val txtCantidad = findViewById<EditText>(R.id.txtCantidad)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)

        //2-Programar el boton de agregar
        btnAgregar.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO ){
                //Guardar datos
                val objConexion = ClaseConexion().cadenaConexion()

                //2- Crear una variable que se igual a una PrepareStatement
                val addProduct = objConexion?.prepareStatement("insert into tbProductos1 values(?, ?, ?)")!!
                addProduct.setString(1,txtNombre.text.toString())
                addProduct.setInt(2,txtPrecio.text.toString().toInt())
                addProduct.setInt(3, txtCantidad.text.toString().toInt())
                addProduct.executeUpdate()
            }



        }




    }
}