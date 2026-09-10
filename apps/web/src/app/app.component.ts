import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Project={id:string;title:string;description:string}; type Chapter={id:string;title:string;position:number}; type Document={id:string;chapterId:string;title:string;type:string;content:string;revision:number;updatedAt:string}; type Workspace={project:Project;chapters:Chapter[];documents:Document[]}; type Version={id:string;revision:number;content:string;createdAt:string}; type Proposal={content:string;authority:string;note:string};
@Component({selector:'app-root',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./app.component.html',styleUrl:'./app.component.css'})
export class AppComponent implements OnInit {
 private http=inject(HttpClient); projects:Project[]=[]; workspace?:Workspace; active?:Document; versions:Version[]=[]; proposal?:Proposal; saving=false; saved=''; panel='assistant';
 ngOnInit(){this.loadProjects();}
 loadProjects(){this.http.get<Project[]>('/api/projects').subscribe(p=>{this.projects=p;if(p[0])this.open(p[0]);});}
 createProject(){const title=prompt('Project title');if(!title?.trim())return;this.http.post<Project>('/api/projects',{title,description:''}).subscribe(p=>{this.projects=[p,...this.projects];this.open(p);});}
 open(p:Project){this.http.get<Workspace>(`/api/projects/${p.id}/workspace`).subscribe(w=>{this.workspace=w;this.active=w.documents[0];this.history();this.proposal=undefined;});}
 select(d:Document){this.active=d;this.history();this.proposal=undefined;}
 addChapter(){if(!this.workspace)return;const title=prompt('Chapter title',`Chapter ${this.workspace.chapters.length+1}`);if(!title)return;this.http.post<Chapter>(`/api/projects/${this.workspace.project.id}/chapters`,{title}).subscribe(()=>this.open(this.workspace!.project));}
 save(){if(!this.workspace||!this.active)return;this.saving=true;this.http.put<Document>(`/api/projects/${this.workspace.project.id}/documents/${this.active.id}`,{title:this.active.title,content:this.active.content}).subscribe(d=>{this.active=d;const i=this.workspace!.documents.findIndex(x=>x.id===d.id);this.workspace!.documents[i]=d;this.saved='Saved '+new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});this.saving=false;this.history();});}
 history(){if(this.workspace&&this.active)this.http.get<Version[]>(`/api/projects/${this.workspace.project.id}/documents/${this.active.id}/versions`).subscribe(v=>this.versions=v);}
 ask(action:string){if(!this.workspace||!this.active)return;this.proposal=undefined;this.http.post<Proposal>(`/api/projects/${this.workspace.project.id}/ai/proposals`,{action,selectedText:this.active.content.slice(-800),instruction:''}).subscribe(x=>this.proposal=x);}
 restore(v:Version){if(!this.active)return;this.active.content=v.content;this.save();}
 words(){return this.active?.content.trim() ? this.active.content.trim().split(/\s+/).length : 0;}
}
