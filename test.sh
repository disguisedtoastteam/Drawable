#! /bin/bash



GoTo()
{
    
    
       find $1 -type f -exec perl -pi -e  's/com.example.myapplication/'$2'/g' {} \;


}
GoTo $1 $2