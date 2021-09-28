// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  // apiUrl: 'https://dockerapp.shyshkin.net',
  // apiUrl: 'https://portal-back-secure.shyshkin.net',
  // apiUrl: 'http://portal-bean.shyshkin.net',
  // apiUrl: 'http://supportportalbackend-env.eba-wfr5wya3.eu-north-1.elasticbeanstalk.com',
  // apiUrl: 'http://support-portal.shyshkin.net:5000',
  apiUrl: 'http://localhost:8080',
  publicUrls: ['/user/login', '/user/register', '/user/*/profile-image', '/user/*/profile-image/**']
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
