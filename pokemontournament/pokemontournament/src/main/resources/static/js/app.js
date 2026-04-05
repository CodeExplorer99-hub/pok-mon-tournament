/* ============================================
   POKEMON TOURNAMENT - JAVASCRIPT COMPLETO
   Gestisce tutta la logica dell'applicazione
   ============================================ */

// ===== VARIABILI GLOBALI: Dati che usiamo in tutta l'app =====
let allPokemon = [];  // Array con TUTTI i Pokemon dal database
let teamA = [];  // Array con i 3 Pokemon della squadra A
let teamB = [];  // Array con i 3 Pokemon della squadra B
let currentBattle = null;  // Battaglia in corso
let battleLog = [];  // Log dei messaggi della battaglia
let allTypes = [];  // Array con tutti i tipi

// ===== VARIABILI BATTAGLIA A TURNI =====
let currentHpP1 = 0;      // HP attuali del Pokemon del giocatore
let currentHpP2 = 0;      // HP attuali del Pokemon della CPU
let maxHpP1 = 0;          // HP massimi del Pokemon del giocatore
let maxHpP2 = 0;          // HP massimi del Pokemon della CPU
let battleInProgress = false;  // Indica se la battaglia è in corso
let currentMatchIndex = 0;     // Indice dello scontro corrente

// ===== API BASE: L'indirizzo del nostro backend Spring Boot =====
const API_BASE = 'http://localhost:8080/api';

// ===== POKEMON IMAGE API: Link pubblico per le immagini dei Pokemon =====
const POKEMON_IMAGE_BASE = 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork';

// ============================================
// FUNZIONE: Ottieni TUTTI i Pokemon dal database
// ============================================
async function loadAllPokemon() {
    try {
        const response = await fetch(`${API_BASE}/pokemon`);
        
        if (!response.ok) {
            throw new Error('Errore nel caricamento dei Pokemon');
        }
        
        allPokemon = await response.json();
        extractAllTypes();
        renderPokemonGrid(allPokemon);
        populateTypeFilter();
        
        console.log('Caricati ' + allPokemon.length + ' Pokemon');
    } catch (error) {
        console.error('Errore caricamento Pokemon:', error);
        alert('Errore nel caricamento dei Pokemon');
    }
}

// ============================================
// FUNZIONE: Estrai tutti i tipi unici
// ============================================
function extractAllTypes() {
    const typesSet = new Set();
    
    allPokemon.forEach(pokemon => {
        if (pokemon.type1 && pokemon.type1.name) {
            typesSet.add(pokemon.type1.name);
        }
        if (pokemon.type2 && pokemon.type2.name) {
            typesSet.add(pokemon.type2.name);
        }
    });
    
    allTypes = Array.from(typesSet).sort();
}

// ============================================
// FUNZIONE: Popola il select filter con i tipi
// ============================================
function populateTypeFilter() {
    const filterSelect = document.getElementById('typeFilter');
    
    allTypes.forEach(type => {
        const option = document.createElement('option');
        option.value = type;
        option.textContent = type.charAt(0).toUpperCase() + type.slice(1);
        filterSelect.appendChild(option);
    });
}

// ============================================
// FUNZIONE: Mostra i Pokemon in grid
// ============================================
function renderPokemonGrid(pokemonList) {
    const grid = document.getElementById('pokemonGrid');
    grid.innerHTML = '';
    
    pokemonList.forEach(pokemon => {
        const card = document.createElement('div');
        card.className = 'pokemon-card';
        card.draggable = true;
        card.dataset.pokemonId = pokemon.id;
        
        card.innerHTML = `
            <img src="${POKEMON_IMAGE_BASE}/${pokemon.pokedexNumber}.png" 
                 alt="${pokemon.name}"
                 onerror="this.src='https://via.placeholder.com/80?text=?'">
            <h4>${pokemon.name}</h4>
            <div class="types">
                <span class="type-badge" style="background-color: ${pokemon.type1.color}">
                    ${pokemon.type1.name.substring(0, 3).toUpperCase()}
                </span>
                ${pokemon.type2 ? `
                    <span class="type-badge" style="background-color: ${pokemon.type2.color}">
                        ${pokemon.type2.name.substring(0, 3).toUpperCase()}
                    </span>
                ` : ''}
            </div>
        `;
        
        card.addEventListener('dragstart', handleDragStart);
        card.addEventListener('dragend', handleDragEnd);
        grid.appendChild(card);
    });
}

// ============================================
// FUNZIONI DRAG AND DROP
// ============================================
let draggedPokemon = null;

function handleDragStart(e) {
    draggedPokemon = e.currentTarget.dataset.pokemonId;
    e.currentTarget.style.opacity = '0.5';
}

function handleDragEnd(e) {
    e.currentTarget.style.opacity = '1';
}

function setupDragAndDropZones() {
    const allSlots = document.querySelectorAll('.roster-slot');
    
    allSlots.forEach(slot => {
        slot.addEventListener('dragover', (e) => {
            e.preventDefault();
            slot.style.backgroundColor = '#e8f4f8';
        });
        
        slot.addEventListener('dragleave', (e) => {
            slot.style.backgroundColor = '';
        });
        
        slot.addEventListener('drop', (e) => {
            e.preventDefault();
            slot.style.backgroundColor = '';
            
            if (!draggedPokemon) return;
            
            const isTeamA = slot.closest('#teamARoster') !== null;
            const slotIndex = Array.from(slot.parentElement.children).indexOf(slot);
            const team = isTeamA ? teamA : teamB;
            
            const pokemon = allPokemon.find(p => p.id === parseInt(draggedPokemon));
            
            if (pokemon) {
                team[slotIndex] = pokemon;
                renderTeamRosters();
                updateTeamStats();
                checkIfBothTeamsReady();
            }
        });
    });
}

// ============================================
// FUNZIONE: Renderizza le squadre
// ============================================
function renderTeamRosters() {
    const teamASlots = document.querySelectorAll('#teamARoster .roster-slot');
    teamASlots.forEach((slot, index) => {
        const pokemon = teamA[index];
        renderSlot(slot, pokemon);
    });
    
    const teamBSlots = document.querySelectorAll('#teamBRoster .roster-slot');
    teamBSlots.forEach((slot, index) => {
        const pokemon = teamB[index];
        renderSlot(slot, pokemon);
    });
}

function renderSlot(slot, pokemon) {
    if (!pokemon) {
        slot.classList.add('empty');
        slot.innerHTML = '';
        return;
    }
    
    slot.classList.remove('empty');
    slot.innerHTML = `
        <img src="${POKEMON_IMAGE_BASE}/${pokemon.pokedexNumber}.png" 
             alt="${pokemon.name}"
             onerror="this.src='https://via.placeholder.com/80?text=?'">
        <div class="roster-slot-info">
            <strong>${pokemon.name}</strong><br>
            HP: ${pokemon.baseHp}
        </div>
    `;
}

// ============================================
// FUNZIONE: Aggiorna statistiche totali delle squadre
// ============================================
function updateTeamStats() {
    updateSingleTeamStats(teamA, 'teamAStats');
    updateSingleTeamStats(teamB, 'teamBStats');
}

function updateSingleTeamStats(team, elementId) {
    const container = document.getElementById(elementId);
    
    if (team.length === 0) {
        container.innerHTML = '<p>Total HP: <span>0</span></p><p>Total Atk: <span>0</span></p>';
        return;
    }
    
    const totalHp = team.reduce((sum, p) => sum + (p.baseHp || 0), 0);
    const totalAtk = team.reduce((sum, p) => sum + (p.baseAttack || 0), 0);
    
    container.innerHTML = `
        <p>Total HP: <span>${totalHp}</span></p>
        <p>Total Atk: <span>${totalAtk}</span></p>
    `;
}

// ============================================
// FUNZIONE: Verifica se entrambe le squadre hanno 3 Pokemon
// ============================================
function checkIfBothTeamsReady() {
    const startBtn = document.getElementById('startTournamentBtn');
    const teamAFull = teamA.length === 3 && teamA.every(p => p);
    const teamBFull = teamB.length === 3 && teamB.every(p => p);
    startBtn.disabled = !(teamAFull && teamBFull);
}

// ============================================
// FUNZIONE: Cerca Pokemon per nome e tipo
// ============================================
function setupSearchAndFilter() {
    const searchInput = document.getElementById('searchInput');
    const typeFilter = document.getElementById('typeFilter');
    
    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        const filtered = allPokemon.filter(pokemon => 
            pokemon.name.toLowerCase().includes(searchTerm) &&
            (typeFilter.value === '' || 
             pokemon.type1.name === typeFilter.value ||
             (pokemon.type2 && pokemon.type2.name === typeFilter.value))
        );
        renderPokemonGrid(filtered);
    });
    
    typeFilter.addEventListener('change', (e) => {
        const searchTerm = searchInput.value.toLowerCase();
        const filtered = allPokemon.filter(pokemon => 
            (searchTerm === '' || pokemon.name.toLowerCase().includes(searchTerm)) &&
            (e.target.value === '' || 
             pokemon.type1.name === e.target.value ||
             (pokemon.type2 && pokemon.type2.name === e.target.value))
        );
        renderPokemonGrid(filtered);
    });
}

// ============================================
// FUNZIONE: Pulisci le squadre
// ============================================
function setupClearButton() {
    document.getElementById('clearTeamsBtn').addEventListener('click', () => {
        teamA = [];
        teamB = [];
        renderTeamRosters();
        updateTeamStats();
        checkIfBothTeamsReady();
    });
}

// ============================================
// FUNZIONE: Avvia il torneo
// ============================================
function setupStartTournamentButton() {
    document.getElementById('startTournamentBtn').addEventListener('click', startTournament);
}

async function startTournament() {
    document.getElementById('draftSection').classList.add('hidden');
    document.getElementById('tournamentSection').classList.remove('hidden');
    generateTournamentBracket();
}

// ============================================
// FUNZIONE: Genera il bracket del torneo
// ============================================
function generateTournamentBracket() {
    const bracket = document.getElementById('tournamentBracket');
    bracket.innerHTML = '';
    
    for (let i = 0; i < 3; i++) {
        const battle = {
            pokemon1: teamA[i],
            pokemon2: teamB[i],
            index: i
        };
        
        const matchupDiv = document.createElement('div');
        matchupDiv.className = 'battle-matchup';
        matchupDiv.innerHTML = `
            <h3>Match ${i + 1}</h3>
            <div class="matchup-pokemon">
                <img src="${POKEMON_IMAGE_BASE}/${battle.pokemon1.pokedexNumber}.png" 
                     alt="${battle.pokemon1.name}"
                     onerror="this.src='https://via.placeholder.com/40?text=?'">
                <strong>${battle.pokemon1.name}</strong>
            </div>
            <div class="vs-text">vs</div>
            <div class="matchup-pokemon">
                <img src="${POKEMON_IMAGE_BASE}/${battle.pokemon2.pokedexNumber}.png" 
                     alt="${battle.pokemon2.name}"
                     onerror="this.src='https://via.placeholder.com/40?text=?'">
                <strong>${battle.pokemon2.name}</strong>
            </div>
            <button class="btn btn-primary" style="width: 100%; margin-top: 1rem;" 
                    onclick="startSingleBattle(${i})">
                Inizia Battaglia
            </button>
        `;
        bracket.appendChild(matchupDiv);
    }
}

// ============================================
// FUNZIONE: Inizia una singola battaglia
// ============================================
function startSingleBattle(matchIndex) {
    const pokemon1 = teamA[matchIndex];  // Giocatore
    const pokemon2 = teamB[matchIndex];  // CPU

    currentMatchIndex = matchIndex;

    currentHpP1 = pokemon1.baseHp;
    currentHpP2 = pokemon2.baseHp;
    maxHpP1 = pokemon1.baseHp;
    maxHpP2 = pokemon2.baseHp;

    battleInProgress = true;

    document.getElementById('tournamentSection').classList.add('hidden');
    document.getElementById('battleSection').classList.remove('hidden');

    displayBattleUI(pokemon1, pokemon2);
    displayMovesButtons(pokemon1, pokemon2);

    document.getElementById('battleLog').innerHTML = '';
    document.getElementById('continueBattleBtn').style.display = 'none';

    addLogMessage(`⚔️ Inizia la battaglia! ${pokemon1.name} VS ${pokemon2.name}!`, 'info');

}

// ============================================
// FUNZIONE: Mostra l'interfaccia della battaglia
// ============================================
function displayBattleUI(pokemon1, pokemon2) {
    // ===== Pokemon 1 (giocatore - sinistra) =====
    document.getElementById('p1Img').src = `${POKEMON_IMAGE_BASE}/${pokemon1.pokedexNumber}.png`;
    document.getElementById('p1Name').textContent = pokemon1.name;
    document.getElementById('p1HP').textContent = `${pokemon1.baseHp}/${pokemon1.baseHp}`;
    document.getElementById('p1HPBar').style.width = '100%';


    const p1TypesDiv = document.getElementById('p1Types');
    p1TypesDiv.innerHTML = `
        <span class="type-badge" style="background-color: ${pokemon1.type1.color}">${pokemon1.type1.name}</span>
        ${pokemon1.type2 ? `<span class="type-badge" style="background-color: ${pokemon1.type2.color}">${pokemon1.type2.name}</span>` : ''}
    `;

    // ===== Pokemon 2 (CPU - destra) =====
    document.getElementById('p2Img').src = `${POKEMON_IMAGE_BASE}/${pokemon2.pokedexNumber}.png`;
    document.getElementById('p2Name').textContent = pokemon2.name;
    document.getElementById('p2HP').textContent = `${pokemon2.baseHp}/${pokemon2.baseHp}`;
    document.getElementById('p2HPBar').style.width = '100%';


    const p2TypesDiv = document.getElementById('p2Types');
    p2TypesDiv.innerHTML = `
        <span class="type-badge" style="background-color: ${pokemon2.type1.color}">${pokemon2.type1.name}</span>
        ${pokemon2.type2 ? `<span class="type-badge" style="background-color: ${pokemon2.type2.color}">${pokemon2.type2.name}</span>` : ''}
    `;
}

// ============================================
// FUNZIONE: Mostra i pulsanti delle mosse del giocatore
// ============================================
function displayMovesButtons(pokemon1, pokemon2) {
    // Cerca o crea il contenitore dei pulsanti mosse
    let movesContainer = document.getElementById('movesContainer');
    if (!movesContainer) {
        movesContainer = document.createElement('div');
        movesContainer.id = 'movesContainer';
        movesContainer.style.cssText = `
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            justify-content: center;
            margin-top: 1rem;
        `;
        // Inserisce i pulsanti sotto il log
        document.querySelector('.battle-log-container').appendChild(movesContainer);
    }

    movesContainer.innerHTML = '<p style="width:100%; text-align:center; font-weight:bold;">Scegli una mossa:</p>';

    const SPECIAL_TYPES = new Set(['Fire','Water','Grass','Electric','Ice','Psychic','Dragon','Dark','Fairy']);

    // Crea un pulsante per ogni mossa del Pokemon del giocatore
    pokemon1.moves.forEach(move => {
        const isSpecial = SPECIAL_TYPES.has(move.type.name);
        const categoryLabel = move.power === 0 ? 'STATO' : (isSpecial ? 'SPC' : 'FIS');
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary';
        btn.style.cssText = 'min-width: 120px; font-size: 0.85rem;';
        btn.innerHTML = `
            <strong>${move.name}</strong><br>
            <small>${move.type.name} | ${categoryLabel} | Pot: ${move.power} | Acc: ${move.accuracy}%</small>
        `;

        btn.addEventListener('click', () => {
            // Quando il giocatore clicca una mossa, esegui il turno
            executeTurn(pokemon1, pokemon2, move);
        });

        movesContainer.appendChild(btn);
    });
}

// ============================================
// FUNZIONE: Esegui un singolo attacco (chiama il backend)
// ============================================
async function executeAttack(attacker, defender, move) {
    const response = await fetch(`${API_BASE}/battle/turn`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            attackerId: attacker.id,
            defenderId: defender.id,
            moveId: move.id
        })
    });
    return await response.json();
}

// ============================================
// FUNZIONE: Testo efficacia per il log
// ============================================
function getEffectivenessText(effectiveness) {
    switch (effectiveness) {
        case 'super':   return ' 🔥 È super efficace!';
        case 'notVery': return ' 💤 Non è molto efficace...';
        case 'immune':  return ' 🛡️ Non ha effetto!';
        default:        return '';
    }
}

// ============================================
// FUNZIONE: Esegui un turno completo
// L'ordine di attacco dipende dalla velocità dei Pokemon.
// ============================================
async function executeTurn(pokemon1, pokemon2, playerMove) {
    if (!battleInProgress) return;

    setMovesButtonsEnabled(false);

    try {
        // Determina ordine di attacco in base alla velocità (come nei giochi originali)
        // In caso di parità, ordine casuale (50/50)
        let playerFaster;
        const spd1 = Number(pokemon1.baseSpeed);
        const spd2 = Number(pokemon2.baseSpeed);
        if (spd1 > spd2) {
            playerFaster = true;
        } else if (spd2 > spd1) {
            playerFaster = false;
        } else {
            playerFaster = Math.random() < 0.5;
        }
        console.log(`[SPEED] ${pokemon1.name}: ${spd1} | ${pokemon2.name}: ${spd2} | playerFirst: ${playerFaster}`);

        if (playerFaster) {
            // ===== GIOCATORE attacca per primo =====
            const playerResult = await executeAttack(pokemon1, pokemon2, playerMove);
            currentHpP2 -= playerResult.damage;
            currentHpP2 = Math.max(0, currentHpP2);
            updateHpBar('p2', currentHpP2, maxHpP2);

            if (playerResult.missed) {
                addLogMessage(`💨 ${pokemon1.name} usa ${playerMove.name}... ma manca il bersaglio!`, 'info');
            } else {
                addLogMessage(
                    `⚔️ ${pokemon1.name} usa ${playerMove.name}! Danno: ${playerResult.damage}${getEffectivenessText(playerResult.effectiveness)}`,
                    'damage'
                );
            }

            if (currentHpP2 <= 0) { endBattle(pokemon1, currentMatchIndex); return; }

            // ===== CPU risponde =====
            const cpuMove = getRandomMove(pokemon2);
            const cpuResult = await executeAttack(pokemon2, pokemon1, cpuMove);
            currentHpP1 -= cpuResult.damage;
            currentHpP1 = Math.max(0, currentHpP1);
            updateHpBar('p1', currentHpP1, maxHpP1);

            if (cpuResult.missed) {
                addLogMessage(`💨 ${pokemon2.name} usa ${cpuMove.name}... ma manca il bersaglio!`, 'info');
            } else {
                addLogMessage(
                    `💥 ${pokemon2.name} usa ${cpuMove.name}! Danno: ${cpuResult.damage}${getEffectivenessText(cpuResult.effectiveness)}`,
                    'damage'
                );
            }

            if (currentHpP1 <= 0) { endBattle(pokemon2, currentMatchIndex); return; }

        } else {
            // ===== CPU attacca per prima (più veloce) =====
            const cpuMove = getRandomMove(pokemon2);
            const cpuResult = await executeAttack(pokemon2, pokemon1, cpuMove);
            currentHpP1 -= cpuResult.damage;
            currentHpP1 = Math.max(0, currentHpP1);
            updateHpBar('p1', currentHpP1, maxHpP1);

            if (cpuResult.missed) {
                addLogMessage(`💨 ${pokemon2.name} usa ${cpuMove.name}... ma manca il bersaglio!`, 'info');
            } else {
                addLogMessage(
                    `💥 ${pokemon2.name} usa ${cpuMove.name}! Danno: ${cpuResult.damage}${getEffectivenessText(cpuResult.effectiveness)}`,
                    'damage'
                );
            }

            if (currentHpP1 <= 0) { endBattle(pokemon2, currentMatchIndex); return; }

            // ===== GIOCATORE risponde =====
            const playerResult = await executeAttack(pokemon1, pokemon2, playerMove);
            currentHpP2 -= playerResult.damage;
            currentHpP2 = Math.max(0, currentHpP2);
            updateHpBar('p2', currentHpP2, maxHpP2);

            if (playerResult.missed) {
                addLogMessage(`💨 ${pokemon1.name} usa ${playerMove.name}... ma manca il bersaglio!`, 'info');
            } else {
                addLogMessage(
                    `⚔️ ${pokemon1.name} usa ${playerMove.name}! Danno: ${playerResult.damage}${getEffectivenessText(playerResult.effectiveness)}`,
                    'damage'
                );
            }

            if (currentHpP2 <= 0) { endBattle(pokemon1, currentMatchIndex); return; }
        }

        setMovesButtonsEnabled(true);

    } catch (error) {
        console.error('Errore nel turno:', error);
        alert('Errore durante la battaglia');
        setMovesButtonsEnabled(true);
    }
}

// ============================================
// FUNZIONE: Sceglie una mossa casuale per la CPU
// ============================================
function getRandomMove(pokemon) {
    const randomIndex = Math.floor(Math.random() * pokemon.moves.length);
    return pokemon.moves[randomIndex];
}

// ============================================
// FUNZIONE: Aggiorna la barra HP
// ============================================
function updateHpBar(player, currentHp, maxHp) {
    const percentage = Math.max(0, (currentHp / maxHp) * 100);
    const hpBar = document.getElementById(`${player}HPBar`);
    const hpText = document.getElementById(`${player}HP`);

    hpBar.style.width = `${percentage}%`;
    hpText.textContent = `${currentHp}/${maxHp}`;

    // Cambia colore in base alla percentuale HP
    if (percentage > 50) {
        hpBar.style.backgroundColor = '#4caf50';  // Verde
    } else if (percentage > 25) {
        hpBar.style.backgroundColor = '#ff9800';  // Arancione
    } else {
        hpBar.style.backgroundColor = '#f44336';  // Rosso
    }
}

// ============================================
// FUNZIONE: Abilita o disabilita i pulsanti delle mosse
// ============================================
function setMovesButtonsEnabled(enabled) {
    const movesContainer = document.getElementById('movesContainer');
    if (!movesContainer) return;
    const buttons = movesContainer.querySelectorAll('button');
    buttons.forEach(btn => btn.disabled = !enabled);
}

// ============================================
// FUNZIONE: Aggiunge un messaggio al log della battaglia
// ============================================
function addLogMessage(message, type) {
    const logDiv = document.getElementById('battleLog');
    const msgDiv = document.createElement('div');
    msgDiv.className = `log-message ${type}`;
    msgDiv.textContent = message;
    logDiv.appendChild(msgDiv);
    logDiv.scrollTop = logDiv.scrollHeight;
}

// ============================================
// FUNZIONE: Fine battaglia - mostra il vincitore
// ============================================
function endBattle(winner, matchIndex) {
    battleInProgress = false;

    // Nascondi i pulsanti delle mosse
    const movesContainer = document.getElementById('movesContainer');
    if (movesContainer) movesContainer.innerHTML = '';

    addLogMessage(`🏆 ${winner.name} vince la battaglia!`, 'info');

    // Salva il vincitore
    currentBattle = { winner, matchIndex };

    // Mostra il pulsante Continue
    document.getElementById('continueBattleBtn').style.display = 'block';
}

// ============================================
// FUNZIONE: Continua alla battaglia successiva
// ============================================
function setupContinueBattleButton() {
    document.getElementById('continueBattleBtn').addEventListener('click', () => {
        document.getElementById('battleSection').classList.add('hidden');
        document.getElementById('tournamentSection').classList.remove('hidden');

        // Aggiorna il bracket con il vincitore
        const matchupDivs = document.querySelectorAll('.battle-matchup');
        matchupDivs[currentBattle.matchIndex].style.backgroundColor = '#e8f4f8';
        matchupDivs[currentBattle.matchIndex].innerHTML += `
            <p style="color: green; font-weight: bold; margin-top: 1rem;">
                ✓ Vincitore: ${currentBattle.winner.name}
            </p>
        `;
    });
}

// ============================================
// FUNZIONE: Torna al draft
// ============================================
function setupBackToDraftButton() {
    document.getElementById('backToDraftBtn').addEventListener('click', () => {
        location.reload();
    });
}

// ============================================
// FUNZIONE: Mostra il vincitore finale
// ============================================
function showWinner(pokemon) {
    document.getElementById('battleSection').classList.add('hidden');
    document.getElementById('tournamentSection').classList.add('hidden');
    document.getElementById('resultsSection').classList.remove('hidden');
    
    const winnerCard = document.getElementById('winnerCard');
    winnerCard.innerHTML = `
        <img src="${POKEMON_IMAGE_BASE}/${pokemon.pokedexNumber}.png" 
             alt="${pokemon.name}"
             onerror="this.src='https://via.placeholder.com/250?text=?'">
        <h3>${pokemon.name}</h3>
        <p>HP: ${pokemon.baseHp}</p>
        <p>Attacco: ${pokemon.baseAttack}</p>
        <p>Difesa: ${pokemon.baseDefense}</p>
        <p>Velocità: ${pokemon.baseSpeed}</p>
    `;
}

// ============================================
// FUNZIONE: Inizializza l'app al caricamento
// ============================================
async function initApp() {
    console.log('Inizializzazione app...');
    
    await loadAllPokemon();
    setupDragAndDropZones();
    setupSearchAndFilter();
    setupClearButton();
    setupStartTournamentButton();
    setupContinueBattleButton();
    setupBackToDraftButton();
    
    console.log('App inizializzata!');
}

// ============================================
// AVVIA L'APP quando la pagina è carica
// ============================================
document.addEventListener('DOMContentLoaded', initApp);