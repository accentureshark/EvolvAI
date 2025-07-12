// github.js
import { log } from './utils.js';
import { BACKEND_URL } from './api.js';

let selectedRepository = null;

/**
 * Initialize GitHub repository functionality
 */
export function setupGitHubRepositories() {
    const refreshBtn = document.getElementById('refresh-repos-btn');
    const orgInput = document.getElementById('org-input');
    const repoList = document.getElementById('repo-list');
    
    if (!refreshBtn || !orgInput || !repoList) {
        log('❌ GitHub repository elements not found');
        return;
    }
    
    // Load repositories on page load
    loadRepositories('accentureshark');
    
    // Refresh button handler
    refreshBtn.addEventListener('click', () => {
        const orgName = orgInput.value.trim();
        if (orgName) {
            loadRepositories(orgName);
        }
    });
    
    // Input change handler
    orgInput.addEventListener('change', () => {
        const orgName = orgInput.value.trim();
        if (orgName) {
            loadRepositories(orgName);
        }
    });
    
    // Repository selection handler
    repoList.addEventListener('change', () => {
        const selectedOption = repoList.options[repoList.selectedIndex];
        if (selectedOption) {
            selectedRepository = {
                name: selectedOption.dataset.name,
                fullName: selectedOption.dataset.fullName,
                cloneUrl: selectedOption.dataset.cloneUrl,
                htmlUrl: selectedOption.dataset.htmlUrl
            };
            log(`📁 Repositorio seleccionado: ${selectedRepository.fullName}`);
        }
    });
}

/**
 * Load repositories for an organization
 * @param {string} orgName - Organization name
 */
function loadRepositories(orgName) {
    const repoList = document.getElementById('repo-list');
    
    if (!repoList) {
        log('❌ Repository list element not found');
        return;
    }
    
    log(`🔄 Cargando repositorios para organización: ${orgName}`);
    
    // Clear existing options
    repoList.innerHTML = '<option>Cargando...</option>';
    
    fetch(`${BACKEND_URL}/api/github/organizations/${orgName}/repositories`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(repositories => {
            log(`📦 Encontrados ${repositories.length} repositorio(s) para ${orgName}`);
            
            // Clear loading message
            repoList.innerHTML = '';
            
            if (repositories.length === 0) {
                repoList.innerHTML = '<option>No hay repositorios públicos disponibles</option>';
                return;
            }
            
            // Add repositories to select
            repositories.forEach(repo => {
                const option = document.createElement('option');
                option.value = repo.name;
                option.textContent = `${repo.name} - ${repo.description || 'Sin descripción'}`;
                option.dataset.name = repo.name;
                option.dataset.fullName = repo.fullName;
                option.dataset.cloneUrl = repo.cloneUrl;
                option.dataset.htmlUrl = repo.htmlUrl;
                repoList.appendChild(option);
            });
            
            // Auto-select quizAI if available
            const quizAIOption = Array.from(repoList.options).find(option => 
                option.dataset.name === 'quizAI' || option.dataset.name === 'quizai'
            );
            if (quizAIOption) {
                quizAIOption.selected = true;
                repoList.dispatchEvent(new Event('change'));
                log('✅ Repositorio quizAI seleccionado automáticamente');
            }
        })
        .catch(error => {
            log(`❌ Error al cargar repositorios: ${error.message}`);
            repoList.innerHTML = '<option>Error al cargar repositorios</option>';
        });
}

/**
 * Get the currently selected repository
 * @returns {object|null} Selected repository object or null
 */
export function getSelectedRepository() {
    return selectedRepository;
}

/**
 * Check if a repository is selected
 * @returns {boolean} True if a repository is selected
 */
export function isRepositorySelected() {
    return selectedRepository !== null;
}