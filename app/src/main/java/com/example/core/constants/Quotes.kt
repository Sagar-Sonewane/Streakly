package com.example.core.constants

import java.util.Calendar
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
            text = "You do not rise to the level of your goals. You fall to the level of your systems.",
            character = "James Clear",
            anime = "Atomic Habits"
        ),
        AnimeQuote(
            text = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            character = "Aristotle",
            anime = "Philosophy"
        ),
        AnimeQuote(
            text = "Discipline is the bridge between goals and accomplishment.",
            character = "Jim Rohn",
            anime = "Self-Discipline"
        ),
        AnimeQuote(
            text = "Success isn't always about greatness. It's about consistency. Consistent hard work leads to success.",
            character = "Dwayne Johnson",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "In a growth mindset, challenges are exciting rather than threatening.",
            character = "Carol Dweck",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "People do not decide their futures, they decide their habits and their habits decide their futures.",
            character = "F.M. Alexander",
            anime = "Habits"
        ),
        AnimeQuote(
            text = "Discipline is choosing between what you want now and what you want most.",
            character = "Abraham Lincoln",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "It's not what we do once in a while that shapes our lives. It's what we do consistently.",
            character = "Tony Robbins",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "A person who never made a mistake never tried anything new.",
            character = "Albert Einstein",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "Success is the sum of small efforts, repeated day in and day out.",
            character = "Robert Collier",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "Without self-discipline, success is impossible, period.",
            character = "Lou Holtz",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "Our habits will make or break us. We become what we repeatedly do.",
            character = "Sean Covey",
            anime = "Habits"
        ),
        AnimeQuote(
            text = "It always seems impossible until it's done.",
            character = "Nelson Mandela",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "Discipline is the refining fire by which talent becomes ability.",
            character = "Roy L. Smith",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "Only the disciplined ones in life are free. If you are undisciplined, you are a slave to your moods and your passions.",
            character = "Eliud Kipchoge",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "We first make our habits, then our habits make us.",
            character = "John Dryden",
            anime = "Habits"
        ),
        AnimeQuote(
            text = "Change is inevitable. Growth is optional.",
            character = "John Maxwell",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "The undisciplined are slaves to passions, appetites, and emotions.",
            character = "Stephen Covey",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "Consistency is what transforms average into excellence.",
            character = "Eric Thomas",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "Life is change. Growth is optional. Choose wisely.",
            character = "Karen Kaiser Clark",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "Sow an act, and you reap a habit; sow a habit, and you reap a character; sow a character, and you reap a destiny.",
            character = "Samuel Smiles",
            anime = "Habits"
        ),
        AnimeQuote(
            text = "Discipline is the soul of an army. It makes small numbers formidable.",
            character = "George Washington",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "I fear not the man who has practiced 10,000 kicks once, but I fear the man who has practiced one kick 10,000 times.",
            character = "Bruce Lee",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "To improve is to change; to be perfect is to change often.",
            character = "Winston Churchill",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "Self-discipline is a form of freedom. Freedom from laziness and lethargy.",
            character = "Harvey Dorfman",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "It's the consistency that makes it art, not the subject.",
            character = "Colleen Hoover",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "We grow up only when we begin to see that our own interest is bound up with that of others.",
            character = "Eleanor Roosevelt",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "With self-discipline, almost anything is possible.",
            character = "Theodore Roosevelt",
            anime = "Discipline"
        ),
        AnimeQuote(
            text = "When we strive to become better than we are, everything around us becomes better too.",
            character = "Paulo Coelho",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "The only person you are destined to become is the person you decide to be.",
            character = "Ralph Waldo Emerson",
            anime = "Growth"
        ),
        AnimeQuote(
            text = "Chains of habit are too light to be felt until they are too heavy to be broken.",
            character = "Warren Buffett",
            anime = "Habits"
        ),
        AnimeQuote(
            text = "Your habits are the method by which you build your identity.",
            character = "James Clear",
            anime = "Atomic Habits"
        ),
        AnimeQuote(
            text = "Small daily improvements over time lead to stunning results.",
            character = "Robin Sharma",
            anime = "Consistency"
        ),
        AnimeQuote(
            text = "Habit is persistence in practice.",
            character = "Octavia Butler",
            anime = "Habits"
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
