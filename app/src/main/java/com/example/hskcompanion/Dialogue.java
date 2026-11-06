package com.example.hskcompanion;

import java.util.List;

public final class Dialogue {
    public final String id,title,a,b,pa,pb,ea,eb,question,feedback;
    public final int level,answer;
    public final List<String> choices;
    public Dialogue(String id,int level,String title,String a,String b,String pa,String pb,
                    String ea,String eb,String question,List<String> choices,int answer,String feedback) {
        this.id=id;this.level=level;this.title=title;this.a=a;this.b=b;
        this.pa=pa;this.pb=pb;this.ea=ea;this.eb=eb;
        this.question=question;this.choices=choices;this.answer=answer;this.feedback=feedback;
    }
}
