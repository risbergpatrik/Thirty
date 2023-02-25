package se.umu.cs.pari0031.thirty1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import se.umu.cs.pari0031.thirty1.Result
import android.widget.Spinner
import android.widget.Toast
import java.util.Collections

class MainActivity : AppCompatActivity() {
    data class SingleDie(var string: String, var value: Int)
    var die1: Die = Die(null)
    var die2: Die = Die(null)
    var die3: Die = Die(null)
    var die4: Die = Die(null)
    var die5: Die = Die(null)
    var die6: Die = Die(null)
    private var score : Score = Score(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    var selectedItem : String = ""
    var round : Int = 10
    var rollsTaken : Int = 0
    var diceToRoll: ArrayList<Int> = arrayListOf()

    //Sparar states av poängen och tärningarnas individuella värden
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("score", score)
        outState.putInt("d1State", die1.value)
        outState.putInt("d2State", die2.value)
        outState.putInt("d3State", die3.value)
        outState.putInt("d4State", die4.value)
        outState.putInt("d5State", die5.value)
        outState.putInt("d6State", die6.value)
        outState.putIntegerArrayList("diceToRollState", diceToRoll)
        outState.putInt("roundState", round)
        outState.putInt("rollsTakenState", rollsTaken)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        die1.image=findViewById<ImageView>(R.id.die1)
        die2.image=findViewById<ImageView>(R.id.die2)
        die3.image=findViewById<ImageView>(R.id.die3)
        die4.image=findViewById<ImageView>(R.id.die4)
        die5.image=findViewById<ImageView>(R.id.die5)
        die6.image=findViewById<ImageView>(R.id.die6)
        val rollButton: Button = findViewById(R.id.rollButton)
        val keepButton: Button = findViewById(R.id.keepButton)
        //Dropdownlistan med val av räknesätt vid poängsättning.
        val dropdown: Spinner = findViewById<Spinner>(R.id.choiceMenu)
        ArrayAdapter.createFromResource(this, R.array.choices, android.R.layout.simple_spinner_item).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dropdown.adapter = adapter
        }
        dropdown.onItemSelectedListener = object :OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedItem = parent?.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        if (savedInstanceState != null) {
            score = savedInstanceState.getParcelable("score")!!
            die1.value = savedInstanceState.getInt("d1State")
            die2.value = savedInstanceState.getInt("d2State")
            die3.value = savedInstanceState.getInt("d3State")
            die4.value = savedInstanceState.getInt("d4State")
            die5.value = savedInstanceState.getInt("d5State")
            die6.value = savedInstanceState.getInt("d6State")
            diceToRoll = savedInstanceState.getIntegerArrayList("diceToRollState") as ArrayList<Int>
            round = savedInstanceState.getInt("roundState")
            rollsTaken = savedInstanceState.getInt("rollsTakenState")
        }

        val roundCounter=findViewById<TextView>(R.id.roundCounter)
        if (round == 10) roundCounter.text=("Runda 1/10")
        else roundCounter.text=("Runda "+(round+1).toString()+"/10")
        val remainingRolls=findViewById<TextView>(R.id.remainingRolls)
        if (rollsTaken>=3) remainingRolls.text=("0 slag kvar")
        else remainingRolls.text=((3-rollsTaken).toString()+" slag kvar")

        setAllImages(diceToRoll)
        setButtonText(rollButton)

        //Skickar med rätt tärningar till rollDice() vid knapptryck.
        rollButton.setOnClickListener {
            rollStart()
            setButtonText(rollButton)
        }

        //Skickar de aktiva tärningsvärdena till metoden calculateScore() vid knapptryck.
        keepButton.setOnClickListener {
            keepScore()
            setButtonText(rollButton)
        }

        //Ser till att metoden toggleDie() körs på rätt tärning när man klickar på den.
        die1.image?.setOnClickListener {
            toggleDie(die1, rollButton)
        }
        die2.image?.setOnClickListener {
            toggleDie(die2, rollButton)
        }
        die3.image?.setOnClickListener {
            toggleDie(die3, rollButton)
        }
        die4.image?.setOnClickListener {
            toggleDie(die4, rollButton)
        }
        die5.image?.setOnClickListener {
            toggleDie(die5, rollButton)
        }
        die6.image?.setOnClickListener {
            toggleDie(die6, rollButton)
        }
    }

    //Companion object till score.
    companion object {
        private const val TAG = "score"
        private const val SCORE_KEY = "MainActivity.score"
    }

    /*Flyttar markerade tärningar till och från den aktiva listan diceToRoll,
    samt reflekterar detta genom att kalla på setButtonText() för att ändra vad som står på knappen
    och byta färg på tärningen så att användaren enkelt ser vilka tärningar som kastas vid knapptryck.*/
    fun toggleDie(die: Die, rollButton: Button) {
        if (die.value != 0) {

            if (!diceToRoll.contains(die.image?.id)) {
                diceToRoll += die.image!!.id
                die.setDieImage(true)
            }
            else {
                diceToRoll -= die.image!!.id
                die.setDieImage(false)
            }
            setButtonText(rollButton)
        }
    }

    //Ser till att alla tärningar visar rätt tärningsbild.
    fun setAllImages(diceToRoll: ArrayList<Int>) {
        var allDice = listOf<Die>(die1, die2, die3, die4, die5, die6)
        for(die in allDice) {
            if (diceToRoll.contains(die.image!!.id)) {
                die.setDieImage(true)
            }
            else {
                die.setDieImage(false)
            }
        }
    }

    //Sätter texten på knappen för att slå tärningar för att bättre reflektera vad som sker när användaren trycker på den.
    fun setButtonText(button: Button) {
        if (round == 10) {
            button.text = "New game"
        }
        else {
            if (diceToRoll.size == 0 || diceToRoll.size == 6) {
                button.text = "Roll all dice"
            } else {
                button.text = "Roll (" + diceToRoll.size + ") dice"
            }
        }
    }

    //Skickar vidare rätt tärningar till metoden som ska slumpa deras värde.
    fun rollStart() {
        if (diceToRoll.size>0 && round!=10) {
            rollDice(diceToRoll)
        }
        else {
            var allDice : List<Int> = listOf(die1.image!!.id,die2.image!!.id,die3.image!!.id,die4.image!!.id,die5.image!!.id,die6.image!!.id)
            rollDice(allDice)
        }
        diceToRoll.clear()
        setAllImages(diceToRoll)
    }

    //För varje tärning i listan slumpas ett värde från 1 till och med 6.
    fun rollDice(dice: List<Int>) {
        if (round == 10) startNewGame()
        if (rollsTaken <4)rollsTaken++
        if (rollsTaken<=3) {
            val remainingRolls=findViewById<TextView>(R.id.remainingRolls)
            remainingRolls.text=((3-rollsTaken).toString()+" slag kvar")
            for (die in dice) {
                var dieView = findViewById<ImageView>(die)
                var dieName = resources.getResourceEntryName(dieView.id)
                when (dieName) {
                    "die1" -> die1.randomize()
                    "die2" -> die2.randomize()
                    "die3" -> die3.randomize()
                    "die4" -> die4.randomize()
                    "die5" -> die5.randomize()
                    "die6" -> die6.randomize()
                }
            }
        }
    }

    //Skickar tärningarnas värde samt det valda räknesättet till scoreobjektet för att räkna ut poängen.
    fun keepScore() {
        var dice = mutableListOf(
            SingleDie("d1", die1.value),
            SingleDie("d2", die2.value),
            SingleDie("d3", die3.value),
            SingleDie("d4", die4.value),
            SingleDie("d5", die5.value),
            SingleDie("d6", die6.value)
        )
        if (score.calculateScore(selectedItem, dice)) {
            advanceRound()
            diceToRoll.clear()
        }
    }

    /*Återställer alla tärningar samt går vidare till nästa runda.
    Om alla rundor är spelade så öppnas resultataktiviteten.
    Reflekterar ett avslutat spel genom att byta text på knappen för att kasta tärning till "New game".*/
    fun advanceRound () {
        die1 = Die(findViewById<ImageView>(R.id.die1))
        die1.image?.setImageResource(android.R.color.transparent)
        die2 = Die(findViewById<ImageView>(R.id.die2))
        die2.image?.setImageResource(android.R.color.transparent)
        die3 = Die(findViewById<ImageView>(R.id.die3))
        die3.image?.setImageResource(android.R.color.transparent)
        die4 = Die(findViewById<ImageView>(R.id.die4))
        die4.image?.setImageResource(android.R.color.transparent)
        die5 = Die(findViewById<ImageView>(R.id.die5))
        die5.image?.setImageResource(android.R.color.transparent)
        die6 = Die(findViewById<ImageView>(R.id.die6))
        die6.image?.setImageResource(android.R.color.transparent)
        round++
        rollsTaken = 0
        val roundCounter=findViewById<TextView>(R.id.roundCounter)
        roundCounter.text=("Runda "+(round+1).toString()+"/10")
        val remainingRolls=findViewById<TextView>(R.id.remainingRolls)
        remainingRolls.text=((3-rollsTaken).toString()+" slag kvar")
        if (round == 10) {
            val intent = Intent(this, Result::class.java)
            intent.putExtra("score", score)
            startActivity(intent)
            setButtonText(findViewById(R.id.rollButton))
        }
    }

    //Återställer både rundan och alla poäng som sparats.
    fun startNewGame () {
        round = 0
        score.resetScore()
    }
}