import { useCallback, useEffect, useState } from "react"
import { Card, Button } from "react-bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';
// import '../App.css';

export default function Notifications() {
  const [projects, setProjects] = useState([]);
  const [loaded, setLoaded] = useState(false);
  const [filtersActivated, setFiltersActivated] = useState([]);

    useEffect(() => {
        
    }, []);

    

    return (
        <div>
            {
                console.log("HI")
            }
            <p>Notifications</p>
        </div>
    );
}
