import React, {Component} from 'react';
import Tabs from './tabs.js';
import './search.css';

export const SearchBar = (props) => (
    <input type="text" placeholder={props.placeholder}/>
);

export class Search extends Component {
    render() {
        return (
            <div>
                <Tabs/>
                <SearchBar placeholder="Search..."/>
                <button type="submit" className="button">
                    SEARCH
                </button>
            </div>
        )
    }
}

export default Search;