
export const ROUTES = {
  LOGIN: '/login',
  HOME: '/home',
  DASHBOARD: '/dashboard',
  PROFILE: '/profile'
};

export const PUBLIC_ROUTES = [
  ROUTES.LOGIN
];

export const PROTECTED_ROUTES = [
  ROUTES.HOME,
  ROUTES.DASHBOARD,
  ROUTES.PROFILE
];
