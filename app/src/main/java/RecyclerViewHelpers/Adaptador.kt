package RecyclerViewHelpers

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.ListaProductos
import samuel.sanchez.myapplication.R
import java.util.UUID

class Adaptador(private var Datos: List<ListaProductos>):RecyclerView.Adapter<ViewHolder> (){


    fun actualizarRecyclerView(nuevaLista: List<ListaProductos>){
        Datos = nuevaLista
        notifyDataSetChanged() //Notifica que hay datos nuevos
    }

    fun actualizarListadoDespuesDeEditar(uuid: String, nuevoNombre: String){
        //Obtener el UUID
        val identificador = Datos.indexOfFirst { it.uuid == uuid }
        //Asigno el nuevo nombre
        Datos [identificador].nombreProducto = nuevoNombre
        //Notifico que los cambios han sido realizados
        notifyItemChanged(identificador)
    }

    //Creamos la funciòn de editar o actualizar en
    fun editarProducto(nombreProducto: String,uuid: String){
        //-Creo una cortina
        GlobalScope.launch (Dispatchers.IO){
            //1- Creo un objrto de la clase conexion
            val objConexion = ClaseConexion().cadenaConexion()

            //2-Creo una varibale que contenga un Preparestatem
            val updateProductos = objConexion?.prepareStatement("update tbProductos1 set nombreProducto  = ? where uuid = ?")!!
            updateProductos.setString(1, nombreProducto)
            updateProductos.setString(2,uuid)
            updateProductos.executeUpdate()

            val commit = objConexion.prepareStatement("commit")
            commit.executeUpdate()

            withContext(Dispatchers.Main){
                actualizarListadoDespuesDeEditar(uuid, nombreProducto)
            }

        }
    }



    //1- Crear la función de eliminar
    fun eliminarRegistro(nombreProducto: String, posicion: Int){
        //Notificar al adaptador
        val listaDatos = Datos.toMutableList()
        listaDatos.removeAt(posicion)

        //Quitar de la base de datos
        GlobalScope.launch (Dispatchers.IO){
            //Dos pasos para eliminar de la base de datos

            //1- Crear un onjeto de la clase conexion
            val objConexion = ClaseConexion().cadenaConexion()

            //2-Creo una variable que contenga um Preparestatement
            val deleteProducto = objConexion?.prepareStatement("delete tbProductos1 where nombreProducto = ?")!!
            deleteProducto.setString(1, nombreProducto)
            deleteProducto.executeUpdate()

            val commit = objConexion.prepareStatement("commit")
            commit.executeUpdate()
        }

        //Notificar el cambio para que refresque la lista
        Datos = listaDatos.toList()
        //Quito los datos de la lista
        notifyItemRemoved(posicion)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val vista = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_card, parent,false)
        return ViewHolder(vista)
    }

    override fun getItemCount() = Datos.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val producto = Datos[position]
        holder.textView.text = producto.nombreProducto

        //Darle click al icono de borrar
        holder.imgBorrar.setOnClickListener {

            //Crear una alerta de confirmación para que se borre
            val context = holder.textView.context

            val builder = AlertDialog.Builder(context)

            builder.setTitle("Eliminar")
            builder.setMessage("¿Estas seguro que deseas eliminar igual el recuerdo siempre vivira en tu memoria?")

            //Boton de mi alerta
            builder.setPositiveButton("si"){
                dialog, wich ->
                eliminarRegistro(producto.nombreProducto, position)

            }

            builder.setNegativeButton("No"){
                dialog, wich ->
                //Si doy en click "No" se cierra la alerta
                dialog.dismiss()
            }
            //Para mostrar la alerta
            val dialog = builder.create()
            dialog.show()

        }

        //Click al icono de editar (lapicito)
        holder.imgEditar.setOnClickListener {
            //Creo una alerta
            val contexto = holder.itemView.context
            val builder = AlertDialog.Builder(contexto)

            builder.setTitle("Editar")

            //Un cuadro de texto donde el usuario escrobira
            // el nuevo nombre
            val cuadritoDeTexto = EditText(contexto)
            cuadritoDeTexto.setHint(producto.nombreProducto)

            //voy a poner el caudrito en el cuadro de alerta
            builder.setView(cuadritoDeTexto)

            //Programamos los botones
            builder.setPositiveButton("Actualizar"){
                dialog, wich ->
                editarProducto(cuadritoDeTexto.text.toString(), producto.uuid)
            }
            builder.setNegativeButton("Cancelar"){
                dialog, wich ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()

        }

    }
}
