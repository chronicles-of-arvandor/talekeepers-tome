package net.arvandor.talekeeper.pronouns

import net.arvandor.talekeeper.player.TtPlayerId

data class TtPronounSet(
    val id: TtPronounSetId,
    val name: String,
    val createdBy: TtPlayerId?,
    val subject: String,
    val `object`: String,
    val dependentPossessive: String,
    val independentPossessive: String,
    val reflexive: String,
) {
    companion object {
        val SHE_HER_HERS = TtPronounSet(
            TtPronounSetId("516e4f0a-b6ff-4295-9fc5-0f903efd0b2b"),
            "she/her/hers",
            null,
            "she",
            "her",
            "her",
            "hers",
            "herself",
        )
        val HE_HIM_HIS = TtPronounSet(
            TtPronounSetId("23b8664b-6cee-41b5-9eb0-822952239753"),
            "he/him/his",
            null,
            "he",
            "him",
            "his",
            "his",
            "himself",
        )
        val THEY_THEM_THEIRS = TtPronounSet(
            TtPronounSetId("c034a39b-e94a-4079-badc-079c8b0b3020"),
            "they/them/theirs",
            null,
            "they",
            "them",
            "their",
            "theirs",
            "themself",
        )
        val IT_IT_ITS = TtPronounSet(
            TtPronounSetId("44a5271e-57e8-43b0-81af-1f33d098ce43"),
            "it/it/its",
            null,
            "it",
            "it",
            "its",
            "its",
            "itself",
        )
    }
}
