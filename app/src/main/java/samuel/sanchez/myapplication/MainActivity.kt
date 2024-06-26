package samuel.sanchez.myapplication

import RecyclerViewHelpers.Adaptador
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.ListaProductos
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.txtProductoDato)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //1-mandar a llamar todos los elemtos de la vista
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtPrecio = findViewById<EditText>(R.id.txtPrecio)
        val txtCantidad = findViewById<EditText>(R.id.txtCantidad)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)
        val rcvDatos = findViewById<RecyclerView>(R.id.rcvDatos)

        //1-Ponerle un layaut a mi RecyclerView
        rcvDatos.layoutManager = LinearLayoutManager(this)

        //2-Crea un adaptador

        ////Funciòn para mostrar datos
        fun obtenerDatos(): List<ListaProductos>{
            val objConexion = ClaseConexion().cadenaConexion()

            val statement = objConexion?.createStatement()
            val resultSet = statement?.executeQuery("select * from tbProductos1")!!

            val listadoProductos = mutableListOf<ListaProductos>()

            //Recorrer todos los datos que me trajo el select

            while (resultSet.next()){
                val uuid = resultSet.getString("uuid")
                val nombre = resultSet.getString("nombreProducto")
                val precio = resultSet.getInt("precio")
                val cantidad = resultSet.getInt("cantidad")
                val producto = ListaProductos(uuid, nombre, precio, cantidad)
                listadoProductos.add(producto)
            }
            return listadoProductos
        }

        //Ejecutamos la funcion
        CoroutineScope(Dispatchers.IO).launch {
            val ejecutarFuncion = obtenerDatos()

            withContext(Dispatchers.Main){
                //Asigno el adaptador mi RecyclerView
                //(Uno mi adaptador con el RecyclerView)
                val miAdaptador = Adaptador(ejecutarFuncion)
                rcvDatos.adapter = miAdaptador
            }
        }




        //2-Programar el boton de agregar
        btnAgregar.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO ){
                //Guardar datos
                val objConexion = ClaseConexion().cadenaConexion()

                //2- Crear una variable que se igual a una PrepareStatement
                val addProduct = objConexion?.prepareStatement("insert into tbProductos1(uuid, nombreProducto, precio, cantidad) values(?, ?, ?,?)")!!
                addProduct.setString(1, UUID.randomUUID().toString())
                addProduct.setString(2,txtNombre.text.toString())
                addProduct.setInt(3,txtPrecio.text.toString().toInt())
                addProduct.setInt(4, txtCantidad.text.toString().toInt())


                addProduct.executeUpdate()

                val nuevosProductos = obtenerDatos()

                //Creo una cortina que actualice el listado
                withContext(Dispatchers.Main){
                    (rcvDatos.adapter as? Adaptador)?.actualizarRecyclerView(nuevosProductos)
                }

            }



        }



        //Mostrar datos




    }
}

