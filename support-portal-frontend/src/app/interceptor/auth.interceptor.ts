import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from "../../environments/environment";
import {AuthenticationService} from "../service/authentication.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private publicUrlPatterns: string[] = [];
  private host: string = environment.apiUrl;

  constructor(private authenticationService: AuthenticationService) {
    let publicUrlsSpringPatterns: string[] = environment.publicUrls;
    let hostPattern = this.host.replace("/", "\/");
    for (const urlPattern of publicUrlsSpringPatterns) {

      let newUrlPattern = urlPattern
        .replace('/**', "\/.*")
        .replace('/*', '\/[A-Za-z0-9\-\.]+')
        +"$";
      this.publicUrlPatterns.push(hostPattern + newUrlPattern);
    }
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    if (this.isRequestUrlMatches(request.url)) {
      request = this.injectAuthToken(request);
    }
    return next.handle(request);
  }

  private injectAuthToken(request: HttpRequest<any>) {
    this.authenticationService.loadToken();
    const token = this.authenticationService.getToken();
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return request;
  }

  public isRequestUrlMatches(requestUrl: string): boolean {
    return requestUrl.includes(this.host) &&
      !this.publicUrlPatterns.some(urlPattern => requestUrl.match(urlPattern));
  }
}
