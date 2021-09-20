package it.bz.noi.community.ui.meet
import androidx.lifecycle.ViewModel

class MeetViewModel : ViewModel() {

    companion object {
        private const val COMPANIES_URL_IT = "https://noi.bz.it/it/aziende"
        private const val STARTUPS_URL_IT = "https://noi.bz.it/it/start-up"
        private const val UNIVERSITY_URL_IT = "https://noi.bz.it/it/universita"
        private const val RESEARCH_URL_IT = "https://noi.bz.it/it/enti-di-ricerca"
        private const val SUPPORT_URL_IT = "https://noi.bz.it/it/istituzioni"
        private const val LAB_URL_IT = "https://noi.bz.it/it/laboratori-sviluppo-tecnologico"
        private const val TEAM_URL_IT = "https://noi.bz.it/it/chi-siamo#t"
    }

    fun getUrlByItemPosition(pos: Int): String {
        return when (pos) {
            0 -> COMPANIES_URL_IT
            1 -> STARTUPS_URL_IT
            2 -> UNIVERSITY_URL_IT
            3 -> RESEARCH_URL_IT
            4 -> SUPPORT_URL_IT
            5 -> LAB_URL_IT
            6 -> TEAM_URL_IT
            else -> throw Exception("Link not found")
        }
    }
}