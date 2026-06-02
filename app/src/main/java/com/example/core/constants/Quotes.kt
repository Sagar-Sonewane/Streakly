package com.example.core.constants

import java.util.Calendar
import java.util.Collections
import java.util.Random

data class AnimeQuote(
    val text: String,
    val character: String,
    val anime: String,
    val imageAsset: String? = null
)

object Quotes {
    val list = listOf(
        AnimeQuote(
            text = "I'll leave tomorrow's problems to tomorrow's me.",
            character = "Saitama",
            anime = "One Punch Man"
        ),
        AnimeQuote(
            text = "Hard work is worthless for those that don't believe in themselves.",
            character = "Naruto Uzumaki",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "If you don't take risks, you can't create a future.",
            character = "Monkey D. Luffy",
            anime = "One Piece"
        ),
        AnimeQuote(
            text = "Power comes in response to a need, not a desire.",
            character = "Goku",
            anime = "Dragon Ball Z"
        ),
        AnimeQuote(
            text = "A person grows up when he's able to overcome hardships.",
            character = "Pain",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "Whatever you lose, you'll find it again. But what you throw away you'll never get back.",
            character = "Kenshin Himura",
            anime = "Rurouni Kenshin"
        ),
        AnimeQuote(
            text = "The only ones who should kill are those who are prepared to be killed.",
            character = "Lelouch",
            anime = "Code Geass"
        ),
        AnimeQuote(
            text = "It's not the face that makes someone a monster, it's the choices they make with their lives.",
            character = "Naruto",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "Do not think about other things, there is only one thing you can do.",
            character = "Roronoa Zoro",
            anime = "One Piece"
        ),
        AnimeQuote(
            text = "People's dreams never end.",
            character = "Marshall D. Teach",
            anime = "One Piece"
        ),
        AnimeQuote(
            text = "Fear is not evil. It tells you what your weakness is.",
            character = "Gildarts Clive",
            anime = "Fairy Tail"
        ),
        AnimeQuote(
            text = "The world isn't perfect. But it's there for us, doing the best it can.",
            character = "Roy Mustang",
            anime = "Fullmetal Alchemist"
        ),
        AnimeQuote(
            text = "A hero is someone who, despite being afraid, still does what's right.",
            character = "All Might",
            anime = "My Hero Academia"
        ),
        AnimeQuote(
            text = "Go beyond. Plus Ultra.",
            character = "All Might",
            anime = "My Hero Academia"
        ),
        AnimeQuote(
            text = "Sometimes I do feel like I'm a failure. But even so... I'm not gonna give up.",
            character = "Izuku Midoriya",
            anime = "My Hero Academia"
        ),
        AnimeQuote(
            text = "The moment you give up is the moment you let someone else win.",
            character = "Koro-sensei",
            anime = "Assassination Classroom"
        ),
        AnimeQuote(
            text = "Giving up kills people. When people reject giving up... they finally win.",
            character = "Alucard",
            anime = "Hellsing"
        ),
        AnimeQuote(
            text = "No matter how hard or impossible it is, never lose sight of your goal.",
            character = "Monkey D. Luffy",
            anime = "One Piece"
        ),
        AnimeQuote(
            text = "Push through the pain. Giving up hurts more.",
            character = "Vegeta",
            anime = "Dragon Ball Z"
        ),
        AnimeQuote(
            text = "A lesson without pain is meaningless. You can't gain anything without sacrificing something.",
            character = "Edward Elric",
            anime = "Fullmetal Alchemist"
        ),
        AnimeQuote(
            text = "If you don't like your destiny, don't accept it. Instead have the courage to change it.",
            character = "Naruto Uzumaki",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "Knowing what it feels to be in pain is exactly why we try to be kind to others.",
            character = "Nagato",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "It's not about whether I can do it. I have to do it.",
            character = "Eren Yeager",
            anime = "Attack on Titan"
        ),
        AnimeQuote(
            text = "If you can't find a reason to fight, then you shouldn't be fighting.",
            character = "Akame",
            anime = "Akame ga Kill"
        ),
        AnimeQuote(
            text = "An excellent leader must be passionate about their work.",
            character = "Erwin Smith",
            anime = "Attack on Titan"
        ),
        AnimeQuote(
            text = "Dedicate your heart.",
            character = "Erwin Smith",
            anime = "Attack on Titan"
        ),
        AnimeQuote(
            text = "Even if I'm worthless and carry demon blood... I refuse to be defeated.",
            character = "Tanjiro Kamado",
            anime = "Demon Slayer"
        ),
        AnimeQuote(
            text = "The strong don't win. The ones who win are strong.",
            character = "Rock Lee",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "Wealth. Fame. Power. One man had it all: the Pirate King Gold Roger.",
            character = "Narrator",
            anime = "One Piece"
        ),
        AnimeQuote(
            text = "There are no shortcuts to any place worth going.",
            character = "Might Guy",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "Prove your worth with your fists.",
            character = "Inosuke Hashibira",
            anime = "Demon Slayer"
        ),
        AnimeQuote(
            text = "A real ninja never gives up on a comrade.",
            character = "Kakashi Hatake",
            anime = "Naruto"
        ),
        AnimeQuote(
            text = "Work hard in silence, let success make the noise.",
            character = "Shoto Todoroki",
            anime = "My Hero Academia"
        ),
        AnimeQuote(
            text = "Don't give up. There's no shame in falling down. The true shame is to not stand up again.",
            character = "Shintaro Midorima",
            anime = "Kuroko's Basketball"
        ),
        AnimeQuote(
            text = "The only thing we're allowed to do is believe that we won't regret the choice we made.",
            character = "Levi Ackerman",
            anime = "Attack on Titan"
        ),
        AnimeQuote(
            text = "If nobody cares to accept you and wants you in this world, accept yourself.",
            character = "Alibaba Saluja",
            anime = "Magi"
        ),
        AnimeQuote(
            text = "Even if things are painful and tough, people should appreciate what it means to be alive.",
            character = "Yato",
            anime = "Noragami"
        ),
        AnimeQuote(
            text = "Being weak is nothing to be ashamed of. Staying weak is.",
            character = "Fuegoleon Vermillion",
            anime = "Black Clover"
        ),
        AnimeQuote(
            text = "You can die anytime, but living takes true courage.",
            character = "Kenshin Himura",
            anime = "Rurouni Kenshin"
        ),
        AnimeQuote(
            text = "Consistency is the proof of effort. Effort is the proof of will.",
            character = "Rock Lee",
            anime = "Naruto"
        )
    )

    fun getQuoteForDay(dayOfYear: Int): AnimeQuote {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        
        // Deterministic shuffle with year as seed
        val shuffled = list.toMutableList()
        val rand = Random(year.toLong())
        // Slower or simple shuffle
        for (i in shuffled.indices.reversed()) {
            val j = rand.nextInt(i + 1)
            val temp = shuffled[i]
            shuffled[i] = shuffled[j]
            shuffled[j] = temp
        }

        val index = (dayOfYear - 1).coerceAtLeast(0) % shuffled.size
        return shuffled[index]
    }
}
