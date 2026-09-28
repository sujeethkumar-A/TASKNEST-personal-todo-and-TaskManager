const API_BASE = '';

const state = {
  currentUser: null,
  taskLists: [],
  tasks: [],
  todayTasks: [],
  overdueTasks: [],
  activeNav: 'dashboard',
  searchText: '',
  priorityFilter: 'all',
  sortBy: 'dueDate',
  selectedListId: null
};

// Boot the page and load the real API data as soon as the browser finishes rendering.
document.addEventListener('DOMContentLoaded', async () => {
  bindUiEvents();
  await restoreSession();
});

function bindUiEvents() {
  document.getElementById('authForm').addEventListener('submit', handleAuthSubmit);
  document.getElementById('authModeToggle').addEventListener('click', toggleAuthMode);
  document.getElementById('logoutBtn').addEventListener('click', logout);
  document.getElementById('taskListForm').addEventListener('submit', handleTaskListSubmit);
  document.getElementById('taskForm').addEventListener('submit', handleTaskSubmit);
  document.getElementById('newTaskBtn').addEventListener('click', openTaskModal);
  document.getElementById('closeTaskModal').addEventListener('click', closeTaskModal);
  document.getElementById('cancelTaskBtn').addEventListener('click', closeTaskModal);
  document.querySelector('.modal-backdrop').addEventListener('click', closeTaskModal);
  document.getElementById('taskSearch').addEventListener('input', (event) => {
    state.searchText = event.target.value.trim().toLowerCase();
    renderTaskResults();
  });

  document.getElementById('taskSort').addEventListener('change', (event) => {
    state.sortBy = event.target.value;
    renderTaskResults();
  });

  document.querySelectorAll('.nav-item').forEach((button) => {
    button.addEventListener('click', () => {
      state.activeNav = button.dataset.nav;
      state.selectedListId = null;
      renderSidebarNavigation();
      renderTaskResults();
      updatePageTitle();
    });
  });

  document.querySelectorAll('.filter-button').forEach((button) => {
    button.addEventListener('click', () => {
      state.priorityFilter = button.dataset.priorityFilter;
      document.querySelectorAll('.filter-button').forEach((item) => {
        item.classList.toggle('active', item === button);
      });
      renderTaskResults();
    });
  });

  document.addEventListener('click', async (event) => {
    const button = event.target.closest('button[data-action]');
    if (!button) return;

    const action = button.dataset.action;
    const taskId = Number(button.dataset.id);

    if (action === 'complete') {
      await updateTaskStatus(taskId, 'complete');
    }

    if (action === 'incomplete') {
      await updateTaskStatus(taskId, 'incomplete');
    }
  });

  document.addEventListener('change', async (event) => {
    const target = event.target;

    if (target.matches('select[data-action="move-task"]')) {
      const taskId = Number(target.dataset.id);
      const taskListId = Number(target.value);

      if (taskListId) {
        await moveTask(taskId, taskListId);
      }
    }
  });
}

async function restoreSession() {
  try {
    const response = await fetch(`${API_BASE}/users/me`);
    if (response.ok) {
      state.currentUser = await response.json();
      await showDashboard();
    } else {
      showAuthView();
    }
  } catch (error) {
    showAuthView();
    setAuthMessage('Unable to connect to TaskNest. Check that the backend is running.');
  }
}

async function handleAuthSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const registerMode = form.dataset.mode === 'register';
  const requestBody = {
    email: document.getElementById('authEmail').value.trim(),
    password: document.getElementById('authPassword').value
  };

  if (registerMode) {
    requestBody.name = document.getElementById('authName').value.trim();
  }

  const endpoint = registerMode ? '/users/register' : '/users/login';
  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      const message = await response.text();
      setAuthMessage(message || (registerMode ? 'Unable to create your account.' : 'Email or password is incorrect.'));
      return;
    }

    state.currentUser = await response.json();
    form.reset();
    await showDashboard();
  } catch (error) {
    setAuthMessage('Unable to connect to TaskNest. Check that the backend is running.');
  }
}

function toggleAuthMode() {
  const form = document.getElementById('authForm');
  const registerMode = form.dataset.mode !== 'register';
  form.dataset.mode = registerMode ? 'register' : 'login';
  document.getElementById('authNameField').hidden = !registerMode;
  document.getElementById('authName').required = registerMode;
  document.getElementById('authPassword').autocomplete = registerMode ? 'new-password' : 'current-password';
  document.getElementById('authTitle').textContent = registerMode ? 'Create your account' : 'Welcome back';
  document.getElementById('authDescription').textContent = registerMode
    ? 'Start organizing your tasks today.'
    : 'Sign in to continue to your tasks.';
  document.getElementById('authSubmit').textContent = registerMode ? 'Create account' : 'Sign in';
  document.getElementById('authModeToggle').textContent = registerMode ? 'I already have an account' : 'Create an account';
  setAuthMessage('');
}

function setAuthMessage(message) {
  document.getElementById('authMessage').textContent = message;
}

function showAuthView() {
  document.getElementById('authView').hidden = false;
  document.getElementById('appShell').hidden = true;
}

async function showDashboard() {
  document.getElementById('authView').hidden = true;
  document.getElementById('appShell').hidden = false;
  document.getElementById('signedInName').textContent = state.currentUser.name;
  await loadTaskLists();
  await loadTasks();
}

async function logout() {
  try {
    await fetch(`${API_BASE}/users/logout`, { method: 'POST' });
  } finally {
    state.currentUser = null;
    state.taskLists = [];
    state.tasks = [];
    showAuthView();
  }
}

async function loadTaskLists() {
  try {
    const response = await fetch(`${API_BASE}/task-lists`);
    if (!response.ok) {
      throw new Error('Failed to load task lists');
    }

    state.taskLists = await response.json();
    renderTaskListSidebar();
    renderTaskListOptions();
  } catch (error) {
    showMessage('Unable to load task lists from the API.', 'error');
  }
}

async function loadTasks() {
  try {
    const [allTasksResponse, todayTasksResponse, overdueTasksResponse] = await Promise.all([
      fetch(`${API_BASE}/tasks`),
      fetch(`${API_BASE}/tasks/today`),
      fetch(`${API_BASE}/tasks/overdue`)
    ]);

    if (!allTasksResponse.ok || !todayTasksResponse.ok || !overdueTasksResponse.ok) {
      throw new Error('Failed to load task data');
    }

    state.tasks = await allTasksResponse.json();
    state.todayTasks = await todayTasksResponse.json();
    state.overdueTasks = await overdueTasksResponse.json();

    renderStats();
    renderOverviewSections();
    renderTaskResults();
    renderSidebarNavigation();
    updatePageTitle();
  } catch (error) {
    showMessage('Unable to load task data from the backend.', 'error');
  }
}

function renderStats() {
  const total = state.tasks.length;
  const today = state.todayTasks.length;
  const overdue = state.overdueTasks.length;
  const completed = state.tasks.filter((task) => task.completed).length;

  document.getElementById('totalTasksCount').textContent = total;
  document.getElementById('todayCount').textContent = today;
  document.getElementById('overdueCount').textContent = overdue;
  document.getElementById('completedCount').textContent = completed;
}

function renderOverviewSections() {
  const todayContainer = document.getElementById('todayTasksOverview');
  todayContainer.innerHTML = state.todayTasks.length
    ? state.todayTasks.map((task) => buildTaskCard(task)).join('')
    : '<div class="empty-state">No tasks scheduled for today.</div>';

  const overdueContainer = document.getElementById('overdueTasksOverview');
  overdueContainer.innerHTML = state.overdueTasks.length
    ? state.overdueTasks.map((task) => buildTaskCard(task)).join('')
    : '<div class="empty-state">No overdue tasks.</div>';
}

function renderSidebarNavigation() {
  document.querySelectorAll('.nav-item').forEach((button) => {
    button.classList.toggle('active', button.dataset.nav === state.activeNav);
  });

  const sidebarButtons = document.querySelectorAll('.sidebar-list-item');
  sidebarButtons.forEach((button) => {
    const matches = Number(button.dataset.listId) === Number(state.selectedListId);
    button.classList.toggle('active', matches);
  });
}

function renderTaskListSidebar() {
  const container = document.getElementById('taskListSidebar');
  container.innerHTML = '';

  if (!state.taskLists.length) {
    container.innerHTML = '<div class="empty-state">No lists yet</div>';
    return;
  }

  state.taskLists.forEach((taskList) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'sidebar-list-item';
    button.dataset.listId = taskList.id;
    button.textContent = taskList.name;
    button.addEventListener('click', () => {
      state.selectedListId = Number(taskList.id);
      state.activeNav = 'my-lists';
      renderSidebarNavigation();
      updatePageTitle();
      renderTaskResults();
    });
    container.appendChild(button);
  });
}

function renderTaskListOptions() {
  const taskListSelect = document.getElementById('taskListSelect');
  taskListSelect.innerHTML = '';

  if (!state.taskLists.length) {
    const option = document.createElement('option');
    option.value = '';
    option.textContent = 'Create a list first';
    taskListSelect.appendChild(option);
    return;
  }

  state.taskLists.forEach((taskList) => {
    const option = document.createElement('option');
    option.value = taskList.id;
    option.textContent = taskList.name;
    taskListSelect.appendChild(option);
  });
}

function updatePageTitle() {
  const titleMap = {
    dashboard: 'Dashboard',
    today: 'Today',
    'my-lists': 'My Lists',
    academics: 'Academics',
    personal: 'Personal',
    overdue: 'Overdue',
    completed: 'Completed'
  };

  const pageTitle = document.getElementById('pageTitle');
  const taskPanelTitle = document.getElementById('taskPanelTitle');

  const label = titleMap[state.activeNav] || 'Dashboard';
  pageTitle.textContent = label;
  taskPanelTitle.textContent = label;
}

function renderTaskResults() {
  const container = document.getElementById('taskResults');
  container.innerHTML = '<div class="empty-state">Loading tasks...</div>';

  const filteredTasks = getFilteredTasks();

  if (!filteredTasks.length) {
    container.innerHTML = '<div class="empty-state">No tasks match the current view.</div>';
    return;
  }

  container.innerHTML = filteredTasks.map((task) => buildTaskCard(task)).join('');
}

function getFilteredTasks() {
  let tasks = [...state.tasks];

  if (state.activeNav === 'today') {
    tasks = tasks.filter((task) => task.dueDate === new Date().toISOString().slice(0, 10));
  } else if (state.activeNav === 'academics') {
    tasks = tasks.filter((task) => getTaskListName(task.taskList).toLowerCase() === 'academics');
  } else if (state.activeNav === 'personal') {
    tasks = tasks.filter((task) => getTaskListName(task.taskList).toLowerCase() === 'personal');
  } else if (state.activeNav === 'overdue') {
    tasks = tasks.filter((task) => !task.completed && isOverdue(task));
  } else if (state.activeNav === 'completed') {
    tasks = tasks.filter((task) => task.completed === true);
  } else if (state.activeNav === 'my-lists' && state.selectedListId) {
    tasks = tasks.filter((task) => Number(task.taskList?.id) === Number(state.selectedListId));
  }

  if (state.searchText) {
    tasks = tasks.filter((task) => (task.title || '').toLowerCase().includes(state.searchText));
  }

  if (state.priorityFilter !== 'all') {
    if (state.priorityFilter === 'completed') {
      tasks = tasks.filter((task) => task.completed === true);
    } else if (state.priorityFilter === 'open') {
      tasks = tasks.filter((task) => task.completed === false);
    } else {
      tasks = tasks.filter((task) => task.priority === state.priorityFilter);
    }
  }

  tasks.sort((a, b) => {
    if (state.sortBy === 'priority') {
      const order = { HIGH: 3, MEDIUM: 2, LOW: 1 };
      return (order[b.priority] || 0) - (order[a.priority] || 0);
    }

    if (state.sortBy === 'title') {
      return (a.title || '').localeCompare(b.title || '');
    }

    const dateA = a.dueDate ? new Date(a.dueDate).getTime() : Number.MAX_SAFE_INTEGER;
    const dateB = b.dueDate ? new Date(b.dueDate).getTime() : Number.MAX_SAFE_INTEGER;
    return dateA - dateB;
  });

  return tasks;
}

function getTaskListName(taskList) {
  if (!taskList) return 'Unassigned';

  const list = state.taskLists.find((item) => Number(item.id) === Number(taskList.id));
  return list ? list.name : taskList.name || 'Unassigned';
}

function isOverdue(task) {
  if (!task.dueDate || task.completed) return false;

  const today = new Date();
  const dueDate = new Date(task.dueDate);
  today.setHours(0, 0, 0, 0);
  dueDate.setHours(0, 0, 0, 0);

  return dueDate < today;
}

function buildTaskCard(task) {
  const taskListName = getTaskListName(task.taskList);
  const dueDate = task.dueDate || 'No date';
  const isLate = isOverdue(task);
  const cardClass = [
    'task-card',
    task.completed ? 'completed' : '',
    isLate && !task.completed ? 'overdue' : ''
  ].filter(Boolean).join(' ');

  const statusClass = task.completed ? 'completed' : isLate ? 'overdue' : 'open';
  const statusText = task.completed ? 'Completed' : isLate ? 'Overdue' : 'Open';

  const actionButton = task.completed
    ? '<button class="secondary-button" type="button" data-action="incomplete" data-id="' + task.id + '">Mark Incomplete</button>'
    : '<button class="success-button" type="button" data-action="complete" data-id="' + task.id + '">Complete</button>';

  const taskListOptions = state.taskLists.map((list) => {
    const selected = Number(task.taskList?.id) === Number(list.id) ? 'selected' : '';
    return `<option value="${list.id}" ${selected}>${escapeHtml(list.name)}</option>`;
  }).join('');

  return `
    <article class="${cardClass}">
      <div class="task-meta">
        <span class="badge ${String(task.priority).toLowerCase()}">${task.priority}</span>
        <span class="status-badge ${statusClass}">${statusText}</span>
      </div>

      <h3>${escapeHtml(task.title)}</h3>

      <div class="task-meta">
        <span>Due date</span>
        <span>${escapeHtml(dueDate)}</span>
      </div>

      <div class="task-meta">
        <span>Task list</span>
        <span>${escapeHtml(taskListName)}</span>
      </div>

      <div class="task-footer">
        <div class="task-actions">
          ${actionButton}
          <select data-action="move-task" data-id="${task.id}" aria-label="Move task">
            ${taskListOptions}
          </select>
        </div>
      </div>
    </article>
  `;
}

async function handleTaskListSubmit(event) {
  event.preventDefault();

  const taskListName = document.getElementById('taskListName').value.trim();

  if (!taskListName) {
    showMessage('Please enter a task list name.', 'error');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/task-lists`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: taskListName
      })
    });

    if (!response.ok) {
      throw new Error('Task list creation failed');
    }

    event.target.reset();
    await loadTaskLists();
    await loadTasks();
    showMessage('Task list created successfully.');
  } catch (error) {
    showMessage('Task list could not be created. Check the backend and try again.', 'error');
  }
}

async function handleTaskSubmit(event) {
  event.preventDefault();

  const title = document.getElementById('taskTitle').value.trim();
  const dueDate = document.getElementById('taskDueDate').value;
  const priority = document.getElementById('taskPriority').value;
  const taskListId = Number(document.getElementById('taskListSelect').value);

  if (!title || !dueDate || !priority || !taskListId) {
    showMessage('Please complete all task fields.', 'error');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/tasks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: title,
        dueDate: dueDate,
        priority: priority,
        taskListId: taskListId
      })
    });

    if (!response.ok) {
      throw new Error('Task creation failed');
    }

    closeTaskModal();
    document.getElementById('taskForm').reset();
    await loadTasks();
    showMessage('Task created successfully.');
  } catch (error) {
    showMessage('Task could not be created. Please check the API response.', 'error');
  }
}

async function updateTaskStatus(taskId, action) {
  try {
    const endpoint = action === 'complete'
      ? `${API_BASE}/tasks/${taskId}/complete`
      : `${API_BASE}/tasks/${taskId}/incomplete`;

    const response = await fetch(endpoint, {
      method: 'PUT'
    });

    if (!response.ok) {
      throw new Error(`Unable to ${action} task`);
    }

    await loadTasks();
    showMessage(`Task marked ${action}.`);
  } catch (error) {
    showMessage('The task status could not be updated.', 'error');
  }
}

async function moveTask(taskId, taskListId) {
  try {
    const response = await fetch(`${API_BASE}/tasks/${taskId}/move/${taskListId}`, {
      method: 'PUT'
    });

    if (!response.ok) {
      throw new Error('Move task failed');
    }

    await loadTasks();
    showMessage('Task moved successfully.');
  } catch (error) {
    showMessage('Task could not be moved.', 'error');
  }
}

function openTaskModal() {
  const taskListSelect = document.getElementById('taskListSelect');

  if (!state.taskLists.length) {
    showMessage('Create a task list before creating a task.', 'error');
    return;
  }

  const modal = document.getElementById('taskModal');
  modal.classList.remove('hidden');
  modal.setAttribute('aria-hidden', 'false');
  taskListSelect.focus();
}

function closeTaskModal() {
  const modal = document.getElementById('taskModal');
  modal.classList.add('hidden');
  modal.setAttribute('aria-hidden', 'true');
}

function showMessage(message, type = 'success') {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.classList.remove('hidden');
  toast.style.background = type === 'error' ? '#b91c1c' : '#111827';

  clearTimeout(showMessage.timeoutId);
  showMessage.timeoutId = setTimeout(() => {
    toast.classList.add('hidden');
  }, 2200);
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
