package br.com.igorbag.githubsearch.ui


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()

    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    private fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)

    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            setupView()
            saveUserLocal(nomeUsuario.text.toString())
            getAllReposByUserName()
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(nome: String) {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPreferences.edit()) {
            putString("nome", nome)
            apply()
        }
    }

    private fun getSharedPreferences(): String {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        return sharedPreferences.getString("nome", "").toString()
    }

    private fun setupCacheResultado(resultado: EditText) {
        val shared = getSharedPreferences()
        resultado.hint = shared
    }

    private fun showUserName() {
        setupCacheResultado(nomeUsuario)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    private fun setupRetrofit() {
        /*
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    private fun getAllReposByUserName() {

        githubApi.getAllRepositoriesByUser(nomeUsuario.text.toString()).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        setupAdapter(it)
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Algo deu errado, tente mais tarde",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Algo deu errado, tente mais tarde",
                    Toast.LENGTH_SHORT
                ).show()

            }
        })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
          passando a listagem dos repositorios
         */
        val adapterRepository = RepositoryAdapter(list)
        adapterRepository.cardItemLister = { repository -> openBrowser(repository.htmlUrl) }
        adapterRepository.btnShareLister = {repository -> shareRepositoryLink(repository.htmlUrl) }
        listaRepositories.adapter = adapterRepository


    }

    // Metodo responsavel por compartilhar o link do repositorio selecionado
       fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio


    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}

