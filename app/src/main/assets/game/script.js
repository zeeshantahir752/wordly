const FLIP_ANIMATION_DURATION = 500
const DANCE_ANIMATION_DURATION = 500
const keyboard = document.querySelector("[data-keyboard]")
const alertContainer = document.querySelector("[data-alert-container]")
const guessGrid = document.querySelector("[data-guess-grid]")

let WORD_LENGTH = 5
let targetWord
let duplicates = ""

startInteraction()
initParams()

function initParams(){
    k_element = document.getElementsByClassName("keyboard")
    large_key = document.getElementsByClassName("large")

    //Set Keyboard
    let alphabets = android.getKeyboard().split("")
    if (android.isRTL()){
        alphabets = alphabets.reverse()
    }
    let lines = ~~(alphabets.length / 10)
    let remaining_letters = alphabets.length - (lines * 10)
    let buttons = ""
    let keys = 0

    //Set Letters
    for (let i = 0; i < alphabets.length - remaining_letters; i++) {
        buttons = buttons + "<button class=\"key\" data-key=\"" + alphabets[keys] + "\">" + alphabets[keys] + "</button>";
        keys++
    }
    for (let i = 0; i < Math.round(remaining_letters / 2); i++) {
        buttons = buttons + "<button class=\"key\" data-key=\"" + alphabets[keys] + "\">" + alphabets[keys] + "</button>";
        keys++
    }
    buttons = buttons + "<button data-enter class=\"key large\">Enter</button>"
    for (let i = 0; i < ~~(remaining_letters / 2); i++) {
        buttons = buttons + "<button class=\"key\" data-key=\"" + alphabets[keys] + "\">" + alphabets[keys] + "</button>";
        keys++
    }
    k_element[0].innerHTML = buttons
    let width = (10 - remaining_letters) * 2
    large_key[0].style.setProperty('grid-column', 'span ' + width) //Change Enter Size

    //Set Letters Number (UI)
    WORD_LENGTH = android.getLettersNumber()
    element = document.getElementsByClassName("guess-grid")
    for (let i = 0; i < WORD_LENGTH * (WORD_LENGTH + 1); i++) {
        var div = document.createElement('div');
        div.className = 'tile';
        element[0].appendChild(div)
    }

    if (android.isRTL()){
        element[0].style.setProperty('direction','rtl')
    }
    let size = 9 - (WORD_LENGTH - 3)
    element[0].style.setProperty('grid-template-columns', 'repeat(' + WORD_LENGTH + ', ' + size + 'em)')
    let trys_count = WORD_LENGTH + 1
    element[0].style.setProperty('grid-template-rows', 'repeat(' + trys_count + ', ' + size + 'em)')

    //Set Target Word
    targetWord = android.getWord()

    //Set Trys
    trys = JSON.parse(android.getTrys())
    if (trys != null)
        trys.forEach(t => {
            let keys = t.split("")
            keys.forEach(k => {
                pressKey(k)
            })
            submitTry()
        })

    if (android.isGameDone()) stopInteraction()
}

function startInteraction() {
    document.addEventListener("click", handleMouseClick)
}

function stopInteraction() {
    document.removeEventListener("click", handleMouseClick)
    document.getElementById("rewind").addEventListener("click", () => rewindKey())
}

function handleMouseClick(e) {
    if (e.target.matches("[data-key]")) {
        pressKey(e.target.dataset.key)
        return
    }

    if (e.target.matches("[data-enter]")) {
        submitGuess()
        return
    }

    if (e.target.matches("[data-delete]")) {
        deleteKey()
        return
    }

    if (e.target.matches("[data-rewind]")) {
        rewindKey()
        return
    }
}

function pressKey(key) {
    const activeTiles = getActiveTiles()
    if (activeTiles.length >= WORD_LENGTH) return
    const nextTile = guessGrid.querySelector(":not([data-letter])")
    nextTile.dataset.letter = key.toLowerCase()
    nextTile.textContent = key
    nextTile.dataset.state = "active"
}

function deleteKey() {
    const activeTiles = getActiveTiles()
    const lastTile = activeTiles[activeTiles.length - 1]
    if (lastTile == null) return
    lastTile.textContent = ""
    delete lastTile.dataset.state
    delete lastTile.dataset.letter
}

function rewindKey() {
    android.rewind()
}

function submitTry() {
  duplicates = ""
  const activeTiles = [...getActiveTiles()]
  const guess = activeTiles.reduce((word, tile) => {
    return word + tile.dataset.letter
  }, "")
  activeTiles.forEach((...params) => setTry(...params, guess))
}

function submitGuess() {
  duplicates = ""
  const activeTiles = [...getActiveTiles()]
  if (activeTiles.length !== WORD_LENGTH) {
    showAlert("Not enough letters")
    shakeTiles(activeTiles)
    return
  }

  const guess = activeTiles.reduce((word, tile) => {
    return word + tile.dataset.letter
  }, "")

  if (!android.wordExists(guess)) {
    showAlert("Not in word list")
    shakeTiles(activeTiles)
    return
  }

  //Add Try
  android.addTry(guess)

  stopInteraction()
  activeTiles.forEach((...params) => flipTile(...params, guess))
}

function setTry(tile, index, array, guess){
  const letter = tile.dataset.letter
  const key = keyboard.querySelector(`[data-key="${letter}"i]`)

  tile.classList.remove("flip")
  if (targetWord[index] === letter) {
    tile.dataset.state = "correct"
    key.classList.add("correct")
  } else if (targetWord.includes(letter) && !duplicatesCheck(letter)) {
    tile.dataset.state = "wrong-location"
    key.classList.add("wrong-location")
  } else {
    tile.dataset.state = "wrong"
    key.classList.add("wrong")
  }

  if (index === array.length - 1) {
    tile.addEventListener(
      "transitionend",
      () => {
        startInteraction()
        checkWinLose(guess, array)
      },
      { once: true }
    )
  }
}

function duplicatesCheck(letter) {
    if (duplicates.includes(letter)) {
        duplicates += letter
        if ((targetWord.match(new RegExp(letter, "g")) || []).length >= (duplicates.match(new RegExp(letter, "g")) || []).length)
            return false
        else return true
    } else {
        duplicates += letter
        return false
    }
}

function flipTile(tile, index, array, guess) {
  const letter = tile.dataset.letter
  const key = keyboard.querySelector(`[data-key="${letter}"i]`)
  setTimeout(() => {
    tile.classList.add("flip")
  }, (index * FLIP_ANIMATION_DURATION) / 2)

  tile.addEventListener(
    "transitionend",
    () => {
      tile.classList.remove("flip")
      if (targetWord[index] === letter) {
        tile.dataset.state = "correct"
        key.classList.add("correct")
      } else if (targetWord.includes(letter) && !duplicatesCheck(letter)) {
        tile.dataset.state = "wrong-location"
        key.classList.add("wrong-location")
      } else {
        tile.dataset.state = "wrong"
        key.classList.add("wrong")
      }

      if (index === array.length - 1) {
        tile.addEventListener(
          "transitionend",
          () => {
            startInteraction()
            checkWinLose(guess, array)
          },
          { once: true }
        )
      }
    },
    { once: true }
  )
}

function getActiveTiles() {
  return guessGrid.querySelectorAll('[data-state="active"]')
}

function showAlert(message, duration = 1000) {
  const alert = document.createElement("div")
  alert.textContent = message
  alert.classList.add("alert")
  alertContainer.prepend(alert)
  if (duration == null) return

  setTimeout(() => {
    alert.classList.add("hide")
    alert.addEventListener("transitionend", () => {
      alert.remove()
    })
  }, duration)
}

function shakeTiles(tiles) {
  tiles.forEach(tile => {
    tile.classList.add("shake")
    tile.addEventListener(
      "animationend",
      () => {
        tile.classList.remove("shake")
      },
      { once: true }
    )
  })
}

function checkWinLose(guess, tiles) {
  if (guess === targetWord) {
    showAlert("You Win", 5000)
    danceTiles(tiles)
    stopInteraction()
    return
  }

  const remainingTiles = guessGrid.querySelectorAll(":not([data-letter])")
  if (remainingTiles.length === 0) {
    showAlert(targetWord.toUpperCase(), null)
    stopInteraction()
  }
}

function danceTiles(tiles) {
  tiles.forEach((tile, index) => {
    setTimeout(() => {
      tile.classList.add("dance")
      tile.addEventListener(
        "animationend",
        () => {
          tile.classList.remove("dance")
        },
        { once: true }
      )
    }, (index * DANCE_ANIMATION_DURATION) / 5)
  })
}
